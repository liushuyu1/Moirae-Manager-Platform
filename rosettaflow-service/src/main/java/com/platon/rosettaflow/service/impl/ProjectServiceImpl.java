package com.platon.rosettaflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platon.rosettaflow.common.constants.SysConstant;
import com.platon.rosettaflow.common.enums.ErrorMsg;
import com.platon.rosettaflow.common.enums.ProjectMemberRoleEnum;
import com.platon.rosettaflow.common.enums.RespCodeEnum;
import com.platon.rosettaflow.common.enums.StatusEnum;
import com.platon.rosettaflow.common.exception.BusinessException;
import com.platon.rosettaflow.common.utils.RedisUtil;
import com.platon.rosettaflow.dto.ProjMemberDto;
import com.platon.rosettaflow.dto.ProjectDto;
import com.platon.rosettaflow.mapper.ProjectMapper;
import com.platon.rosettaflow.mapper.domain.*;
import com.platon.rosettaflow.service.*;
import com.platon.rosettaflow.service.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 * @date 2021/8/16
 */
@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    @Resource
    IProjectMemberService projectMemberService;

    @Resource
    private IWorkflowTempService workflowTempService;

    @Resource
    private IWorkflowService workflowService;

    @Resource
    private IWorkflowNodeTempService workflowNodeTempService;

    @Resource
    private IWorkflowNodeService workflowNodeService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CommonService commonService;

    @Resource
    private IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProject(ProjectDto projectDto) {
        try {
            Long userId = UserContext.get().getId();
            if (userId == null || userId == 0L) {
                log.error("ProjectServiceImpl->addProject fail user un login");
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_UN_LOGIN.getMsg());
            }
            Project project = new Project();
            project.setUserId(userId);
            project.setProjectName(projectDto.getProjectName());
            project.setProjectDesc(projectDto.getProjectDesc());
            this.save(project);
            // 默认当前用户为管理员
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProjectId(project.getId());
            projectMember.setUserId(userId);
            projectMember.setRole(ProjectMemberRoleEnum.ADMIN.getRoleId());
            addProjMember(projectMember);

            // 如果项目模板ID不为空，将项目模板中的工作流复制到当前项目
            if (null != projectDto.getProjectTempId() && projectDto.getProjectTempId() > 0) {
                WorkflowTemp workflowTemp = workflowTempService.getWorkflowTemplate(projectDto.getProjectTempId());
                if (null != workflowTemp) {
                    // 添加工作流
                    Long workflowId = workflowService.addWorkflowByTemplate(project.getId(), userId, workflowTemp);

                    //从工作流节点模板中复制所有工作流节点
                    List<WorkflowNodeTemp> workflowNodeTempList = workflowNodeTempService.getByWorkflowTempId(workflowTemp.getId());

                    //根据工作流节点模板列表，添加工作流节点
                    if (workflowNodeTempList.size() > 0) {
                        workflowNodeService.addWorkflowNodeByTemplate(workflowId, workflowNodeTempList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("addProject--新增项目信息失败, 错误信息:{}, 异常:{}", e.getMessage(), e);
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.PROJECT_NAME_EXISTED.getMsg());
            }
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.ADD_PROJ_ERROR.getMsg());
        }
    }

    @Override
    public void updateProject(Project project) {
        checkAdminPermission(project.getId());
        try {
            this.updateById(project);
        } catch (Exception e) {
            log.error("updateProject--修改项目信息失败, 错误信息:{}", e.getMessage());
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.PROJECT_NAME_EXISTED.getMsg());
            }
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.UPDATE_PROJ_ERROR.getMsg());
        }
    }

    @Override
    public IPage<ProjectDto> queryProjectPageList(String projectName, Long current, Long size) {
        try {
            Long userId = commonService.getCurrentUser().getId();
            IPage<ProjectDto> page = new Page<>(current, size);
            return this.baseMapper.queryProjectPageList(userId, projectName, page);
        } catch (Exception e) {
            log.error("queryProjectList--查询项目列表失败, 错误信息:{}", e.getMessage());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.QUERY_PROJ_LIST_ERROR.getMsg());
        }

    }

    @Override
    public Project queryProjectDetails(Long id) {
        try {
            LambdaQueryWrapper<Project> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(Project::getId, id);
            queryWrapper.eq(Project::getStatus, StatusEnum.VALID.getValue());
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            log.error("queryProjectDetails--查询项目详情失败, 错误信息:{}", e.getMessage());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.QUERY_PROJ_DETAILS_ERROR.getMsg());
        }

    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteProject(Long id) {
        checkAdminPermission(id);
        // 逻辑删除工作流
        List<Workflow> workflowList = workflowService.queryWorkFlowByProjectId(id);
        if (workflowList != null && workflowList.size() > 0) {
            workflowList.parallelStream().forEach(workflow -> {
                workflowService.deleteWorkflow(workflow.getId());
                if (workflow.getNodeNumber() > 0) {
                    // 物理删除工作流节点
                    List<WorkflowNode> workflowNodeList = workflowNodeService.getWorkflowNodeList(workflow.getId());
                    if (workflowNodeList != null && workflowNodeList.size() > 0) {
                        workflowNodeList.parallelStream().forEach(workflowNode ->
                            workflowNodeService.deleteWorkflowNode(workflowNode.getId()));
                    }
                }
            });
        }
        // 根据项目id获取成员id
        List<Long> idList = getMemberIdByProjectId(id);
        // 批量物理删除项目成员
        projectMemberService.removeByIds(idList);

        // 逻辑删除项目信息,修改项目版本标识
        this.updateBatchById(updateDelVersionById(Collections.singletonList(id)));
    }

    /**
     * 根据项目id获取成员id
     */
    private List<Long> getMemberIdByProjectId(Long projectId) {
        List<ProjectMember> projectMemberList = projectMemberService.queryByProjectId(projectId);
        if (projectMemberList == null || projectMemberList.size() == 0) {
            return new ArrayList<>();
        }
        List<Long> idList = new ArrayList<>();
        projectMemberList.forEach(projectMember -> idList.add(projectMember.getId()));
        return idList;
    }

    /**
     * 修改版本标识，解决逻辑删除唯一校验问题
     */
    private List<Project> updateDelVersionById(List<Long> idList) {
        List<Project> projectList = new ArrayList<>();
        if (idList != null && idList.size() > 0) {
            idList.parallelStream().forEach(id -> {
                Project project = new Project();
                project.setId(id);
                project.setDelVersion(id);
                project.setStatus((byte) 0);
                projectList.add(project);
            });
        }
        return projectList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjectBatch(String ids) {
        // 转换id类型
        List<Long> idsList = convertIdType(ids);
        for (Long id : idsList) {
            checkAdminPermission(id);
        }
        // 删除项目成员
        LambdaQueryWrapper<ProjectMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ProjectMember::getProjectId, idsList);
        List<ProjectMember> projectMemberList = projectMemberService.getBaseMapper().selectList(queryWrapper);
        List<Long> memberIdsList = new ArrayList<>();
        if (projectMemberList != null && projectMemberList.size() > 0) {
            for (ProjectMember projectMember : projectMemberList) {
                memberIdsList.add(projectMember.getId());
            }
            // 物理删除项目成员
            projectMemberService.removeByIds(memberIdsList);
        }
        // 逻辑删除项目信息，修改版本标识
        this.updateBatchById(updateDelVersionById(idsList));
    }

    @Override
    public IPage<ProjMemberDto> queryProjMemberPageList(Long projectId, String userName, Long current, Long size) {
        IPage<ProjMemberDto> iPage = new Page<>(current, size);
        return this.baseMapper.queryProjMemberList(projectId, userName, iPage);
    }

    @Override
    public void addProjMember(ProjectMember projectMember) {
        if (null != projectMember.getProjectId()) {
            checkAdminPermission(projectMember.getProjectId());
        }
        try {
            projectMemberService.save(projectMember);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.MEMBER_ROLE_EXISTED.getMsg());
        }
    }

    @Override
    public void updateProjMember(ProjectMember projectMember) {
        ProjectMember oldMember = projectMemberService.getById(projectMember.getId());

        //管理员只有一个时，不能更改管理员的角色
        List<ProjectMember> projectMemberList = projectMemberService.getAdminList(oldMember.getProjectId());
        if (projectMemberList.size() == 1 && projectMemberList.get(0).getRole() != projectMember.getRole().byteValue()) {
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_ADMIN_must__ERROR.getMsg());
        }
        // 校验项目成员角色
        checkAdminPermission(oldMember.getProjectId());
        // 删除旧的项目成员缓存
        deleteRedisRole(oldMember.getUserId(), oldMember.getProjectId());
        try {
            projectMemberService.updateById(projectMember);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.MEMBER_ROLE_EXISTED.getMsg());
        }
        // 新增新的项目成员角色缓存
        redisUtil.set(StrUtil.format(SysConstant.REDIS_USER_PROJECT_ROLE_KEY,
                projectMember.getUserId(), oldMember.getProjectId()), projectMember.getRole());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteProjMember(Long projMemberId) {
        ProjectMember projectMember = projectMemberService.queryById(projMemberId);
        checkAdminPermission(projectMember.getProjectId());
        deleteRedisRole(projectMember.getUserId(), projectMember.getProjectId());
        projectMemberService.removeById(projMemberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjMemberBatch(String projMemberIds) {
        List<Long> idList = convertIdType(projMemberIds);
        idList.parallelStream().forEach(id -> {
            ProjectMember projectMember = projectMemberService.queryById(id);
            checkAdminPermission(projectMember.getProjectId());
            deleteRedisRole(projectMember.getUserId(), projectMember.getProjectId());
        });
        projectMemberService.removeByIds(idList);
    }

    @Override
    public Byte getRoleByProjectId(Long projectId) {
        Long userId = commonService.getCurrentUser().getId();
        Object role = redisUtil.get(StrUtil.format(SysConstant.REDIS_USER_PROJECT_ROLE_KEY, userId, projectId));
        if (null != role) {
            return ((Integer) role).byteValue();
        } else {
            ProjectMember projectMember = projectMemberService.queryByProjectIdAndUserId(userId, projectId);
            if (null != projectMember) {
                redisUtil.set(StrUtil.format(SysConstant.REDIS_USER_PROJECT_ROLE_KEY, userId, projectId), projectMember.getRole());
                return projectMember.getRole();
            } else {
                return null;
            }
        }
    }

    /** 转换id类型 */
    @Override
    public List<User> queryAllUserNickName(Long projectId) {
        return userService.queryUserByProjectId(projectId);
    }

    /**
     * 转换id类型
     */
    private List<Long> convertIdType(String ids) {
        return Arrays.stream(ids.split(",")).map(id ->
                Long.parseLong(id.trim())).collect(Collectors.toList());
    }

    /**
     * 根据用户id及项目id删除缓存在redis中的角色信息
     *
     * @param userId    userId
     * @param projectId projectId
     */
    private void deleteRedisRole(Long userId, Long projectId) {
        Object key = redisUtil.get(StrUtil.format(SysConstant.REDIS_USER_PROJECT_ROLE_KEY, userId, projectId));
        if (null != key) {
            redisUtil.delete(StrUtil.format(SysConstant.REDIS_USER_PROJECT_ROLE_KEY, userId, projectId));
        }
    }

    private void checkAdminPermission(Long projectId) {
        Byte role = this.getRoleByProjectId(projectId);
        if (null != role && ProjectMemberRoleEnum.ADMIN.getRoleId() != role) {
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_ADMIN_PERMISSION_ERROR.getMsg());
        }
    }

}