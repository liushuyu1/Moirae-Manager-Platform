package com.moirae.rosettaflow.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moirae.rosettaflow.dto.ProjMemberDto;
import com.moirae.rosettaflow.dto.ProjectDto;
import com.moirae.rosettaflow.dto.ProjectModelDto;
import com.moirae.rosettaflow.mapper.domain.Project;
import com.moirae.rosettaflow.mapper.domain.ProjectMember;
import com.moirae.rosettaflow.mapper.domain.User;
import com.moirae.rosettaflow.req.project.*;
import com.moirae.rosettaflow.service.IProjectService;
import com.moirae.rosettaflow.utils.ConvertUtils;
import com.moirae.rosettaflow.vo.PageVo;
import com.moirae.rosettaflow.vo.ResponseVo;
import com.moirae.rosettaflow.vo.project.*;
import com.moirae.rosettaflow.vo.user.UserNicknameVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 项目管理关接口
 *
 * @author admin
 * @date 2021/8/16
 */
@Slf4j
@RestController
@Api(tags = "项目管理关接口")
@RequestMapping(value = "project", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    @Resource
    private IProjectService projectService;

    @GetMapping("queryProjectPageList")
    @ApiOperation(value = "查询项目列表", notes = "查询项目列表")
    public ResponseVo<PageVo<ProjectListVo>> queryProjectPageList(@Valid ProjListReq projListReq) {
        IPage<ProjectDto> iPage = projectService.queryProjectPageList(projListReq.getProjectName(), projListReq.getCurrent(), projListReq.getSize());
        List<ProjectListVo> items = BeanUtil.copyToList(iPage.getRecords(), ProjectListVo.class);
        return ResponseVo.createSuccess(ConvertUtils.convertPageVo(iPage, items));
    }

    @GetMapping("queryProjectDetails")
    @ApiOperation(value = "查询项目详情", notes = "查询项目详情")
    public ResponseVo<ProjectDetailsVo> queryProjectDetails(@Validated ProjDetailsReq projDetailsReq) {
        Project project = projectService.queryProjectDetails(projDetailsReq.getId());
        return ResponseVo.createSuccess(BeanUtil.copyProperties(project, ProjectDetailsVo.class));
    }

    @PostMapping("addProject")
    @ApiOperation(value = "新增项目", notes = "新增项目")
    public ResponseVo<ProjectVo> addProject(@RequestBody @Valid AddProjectReq addProjectReq, HttpServletRequest request) {
        // 获取语言类型
        String language = request.getHeader("Accept-Language");
        Long id = projectService.addProject(BeanUtil.copyProperties(addProjectReq, ProjectDto.class), language);
        return ResponseVo.createSuccess(new ProjectVo(id));
    }

    @PostMapping("updateProject")
    @ApiOperation(value = "修改项目", notes = "修改项目")
    public ResponseVo<?> updateProject(@RequestBody @Valid UpdateProjectReq updateProjectReq) {
        projectService.updateProject(BeanUtil.copyProperties(updateProjectReq, Project.class));
        return ResponseVo.createSuccess();
    }

    @PostMapping("deleteProject")
    @ApiOperation(value = "删除项目", notes = "删除项目")
    public ResponseVo<?> deleteProject(@RequestBody @Valid ProjDetailsReq projDetailsReq) {
        projectService.deleteProject(projDetailsReq.getId());
        return ResponseVo.createSuccess();
    }

    @PostMapping("deleteProjectBatch")
    @ApiOperation(value = "批量删除项目", notes = "批量删除项目")
    public ResponseVo<?> deleteProjectBatch(@RequestBody @Valid DeleteProjBatchReq deleteBatchReq) {
        projectService.deleteProjectBatch(deleteBatchReq.getIds());
        return ResponseVo.createSuccess();
    }

    @GetMapping("queryProjMemberPageList")
    @ApiOperation(value = "查询项目成员列表", notes = "查询项目成员列表")
    public ResponseVo<PageVo<ProjMemberListVo>> queryProjMemberList(@Valid ProjMemberListReq listReq) {
        IPage<ProjMemberDto> iPage = projectService.queryProjMemberPageList(listReq.getProjectId(),
                listReq.getUserName(), listReq.getCurrent(), listReq.getSize());
        List<ProjMemberListVo> items = BeanUtil.copyToList(iPage.getRecords(), ProjMemberListVo.class);
        return ResponseVo.createSuccess(ConvertUtils.convertPageVo(iPage, items));
    }

    @PostMapping("addProjMember")
    @ApiOperation(value = "新增项目成员", notes = "新增项目成员")
    public ResponseVo<?> addProjMember(@RequestBody @Valid AddMemberReq addMemberReq) {
        ProjectMember projectMember = BeanUtil.toBean(addMemberReq, ProjectMember.class);
        projectService.addProjMember(projectMember);
        return ResponseVo.createSuccess();
    }

    @PostMapping("updateProjMember")
    @ApiOperation(value = "修改项目成员", notes = "修改项目成员")
    public ResponseVo<?> updateProjMember(@RequestBody @Valid UpdateMemberReq updateMemberReq) {
        ProjectMember projectMember = BeanUtil.toBean(updateMemberReq, ProjectMember.class);
        projectService.updateProjMember(projectMember);
        return ResponseVo.createSuccess();
    }

    @PostMapping("deleteProjMember")
    @ApiOperation(value = "删除项目成员", notes = "删除项目成员")
    public ResponseVo<?> deleteProjMember(@RequestBody @Valid DeleteMemberReq deleteMemberReq) {
        projectService.deleteProjMember(deleteMemberReq.getProjMemberId());
        return ResponseVo.createSuccess();
    }

    @PostMapping("deleteProjMemberBatch")
    @ApiOperation(value = "批量删除项目成员", notes = "批量删除项目成员")
    public ResponseVo<?> deleteProjMemberBatch(@RequestBody @Valid DeleteMemberBatchReq deleteBatchReq) {
        projectService.deleteProjMemberBatch(deleteBatchReq.getProjMemberIds());
        return ResponseVo.createSuccess();
    }

    @GetMapping("queryAllUserNickname/{projectId}")
    @ApiOperation(value = "查询当前项目可以筛选的用户", notes = "查询当前项目可以筛选的用户")
    public ResponseVo<List<UserNicknameVo>> queryAllUserNickname(@ApiParam(value = "项目ID", required = true) @PathVariable Long projectId) {
        List<User> list = projectService.queryAllUserNickName(projectId);
        return ResponseVo.createSuccess(BeanUtil.copyToList(list, UserNicknameVo.class));
    }

    @GetMapping("queryCurrentProjAlgModel")
    @ApiOperation(value = "查询当前项目的算法模型", notes = "查询当前项目的算法模型")
    public ResponseVo<List<ProjectModelVo>> queryCurrentProjAlgModel(@Valid ProjAlgModel projAlgModel, HttpServletRequest request) {
        // 获取语言类型
        String language = request.getHeader("Accept-Language");
        List<ProjectModelDto> list = projectService.queryCurrentProjAlgModel(projAlgModel.getProjectId(), projAlgModel.getAlgorithmId(), language);
        return ResponseVo.createSuccess(BeanUtil.copyToList(list, ProjectModelVo.class));
    }

}
