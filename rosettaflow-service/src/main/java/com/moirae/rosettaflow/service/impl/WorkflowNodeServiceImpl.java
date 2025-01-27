package com.moirae.rosettaflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moirae.rosettaflow.common.constants.SysConstant;
import com.moirae.rosettaflow.common.enums.*;
import com.moirae.rosettaflow.common.exception.BusinessException;
import com.moirae.rosettaflow.dto.AlgorithmDto;
import com.moirae.rosettaflow.dto.WorkflowNodeDto;
import com.moirae.rosettaflow.grpc.identity.dto.NodeIdentityDto;
import com.moirae.rosettaflow.grpc.service.GrpcAuthService;
import com.moirae.rosettaflow.mapper.WorkflowNodeMapper;
import com.moirae.rosettaflow.mapper.domain.*;
import com.moirae.rosettaflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static cn.hutool.core.date.DateTime.now;

/**
 * 工作流节点服务实现类
 *
 * @author hudenian
 * @date 2021/8/31
 */
@Slf4j
@Service
public class WorkflowNodeServiceImpl extends ServiceImpl<WorkflowNodeMapper, WorkflowNode> implements IWorkflowNodeService {

    @Resource
    private IProjectService projectService;

    @Resource
    private IWorkflowService workflowService;

    @Resource
    private IAlgorithmService algorithmService;

    @Resource
    private IJobService jobService;

    @Resource
    private IWorkflowNodeCodeService workflowNodeCodeService;

    @Resource
    private IWorkflowNodeInputService workflowNodeInputService;

    @Resource
    private IWorkflowNodeOutputService workflowNodeOutputService;

    @Resource
    private IWorkflowNodeVariableService workflowNodeVariableService;

    @Resource
    private IWorkflowNodeResourceService workflowNodeResourceService;

    @Resource
    private GrpcAuthService grpcAuthService;

    @Resource
    private IOrganizationService organizationService;

    @Resource
    private IUserMetaDataService userMetaDataService;

    @Override
    public List<WorkflowNodeDto> queryNodeDetailsList(Long id, String language) {
        // 获取工作流节点列表
        List<WorkflowNode> workflowNodeList = getWorkflowNodeList(id);
        if (workflowNodeList == null || workflowNodeList.size() == 0) {
            return new ArrayList<>();
        }
        List<WorkflowNodeDto> workflowNodeDtoList = new ArrayList<>();
        for (WorkflowNode workflowNode : workflowNodeList) {
            // 工作流节点dto
            WorkflowNodeDto workflowNodeDto = BeanUtil.toBean(workflowNode, WorkflowNodeDto.class);
            // 算法对象
            AlgorithmDto algorithmDto = algorithmService.queryAlgorithmDetails(workflowNode.getAlgorithmId());
            if (Objects.nonNull(algorithmDto)) {
                // 处理国际化语言
                if (SysConstant.EN_US.equals(language)) {
                    algorithmDto.setAlgorithmName(algorithmDto.getAlgorithmNameEn());
                    algorithmDto.setAlgorithmDesc(algorithmDto.getAlgorithmDescEn());
                }
                // 工作流节点算法代码, 如果可查询出，表示已修改，否则没有变动
                WorkflowNodeCode workflowNodeCode = workflowNodeCodeService.getByWorkflowNodeId(workflowNode.getId());
                if (Objects.nonNull(workflowNodeCode)) {
                    algorithmDto.setEditType(workflowNodeCode.getEditType());
                    algorithmDto.setCalculateContractCode(workflowNodeCode.getCalculateContractCode());
                }
                // 工作流节点算法资源环境, 如果可查询出，表示已修改，否则没有变动
                WorkflowNodeResource nodeResource = workflowNodeResourceService.getByWorkflowNodeId(workflowNode.getId());
                if (Objects.nonNull(nodeResource)) {
                    if (null != nodeResource.getCostCpu()) {
                        algorithmDto.setCostCpu(nodeResource.getCostCpu());
                    }
                    if (null != nodeResource.getCostGpu()) {
                        algorithmDto.setCostGpu(nodeResource.getCostGpu());
                    }
                    if (null != nodeResource.getCostMem()) {
                        algorithmDto.setCostMem(nodeResource.getCostMem());
                    }
                    if (null != nodeResource.getCostBandwidth()) {
                        algorithmDto.setCostBandwidth(nodeResource.getCostBandwidth());
                    }
                    if (null != nodeResource.getRunTime()) {
                        algorithmDto.setRunTime(nodeResource.getRunTime());
                    }
                }
            }
            workflowNodeDto.setAlgorithmDto(algorithmDto);
            //工作流节点输入列表
            List<WorkflowNodeInput> workflowNodeInputList = workflowNodeInputService.getByWorkflowNodeId(workflowNode.getId());
            workflowNodeDto.setWorkflowNodeInputList(workflowNodeInputList);
            //工作流节点输出列表
            List<WorkflowNodeOutput> workflowNodeOutputList = workflowNodeOutputService.getByWorkflowNodeId(workflowNode.getId());
            workflowNodeDto.setWorkflowNodeOutputList(workflowNodeOutputList);
            workflowNodeDtoList.add(workflowNodeDto);
        }
        return workflowNodeDtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWorkflowAllNodeData(Long workflowId, List<WorkflowNodeDto> workflowNodeDtoList) {
        if (null == workflowNodeDtoList || workflowNodeDtoList.size() == 0) {
            log.error("saveWorkflowAllNodeData--工作流节点信息workflowNodeDtoList不能为空");
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_NODE_NOT_EXIST.getMsg());
        }
        Workflow workflow = workflowService.queryWorkflowDetail(workflowId);
        // 校验是否有编辑权限
        checkEditPermission(workflow.getProjectId());
        // 判断当前工作流是否存在正在运行或者有加入正在运行的作业中，有则不让保存
        if (workflow.getRunStatus() == WorkflowRunStatusEnum.RUNNING.getValue()) {
            log.error("saveWorkflowNode--工作流运行中:{}", JSON.toJSONString(workflow));
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_RUNNING_EXIST.getMsg());
        }
        // 判断正在运行的作业是否包含此工作流
        List<Job> jobList = jobService.listRunJobByWorkflowId(workflowId);
        if (null != jobList && jobList.size() > 0) {
            log.error("saveWorkflowNode--工作流运行中:{}", JSON.toJSONString(workflow));
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_RUNNING_EXIST.getMsg());
        }
        
        // 校验节点配置输入是否有过期数据
        this.checkExpireConfigData(workflowNodeDtoList);

        // 校验只有一个算法节点，且该节点需要模型时，是否传入模型参数
        this.checkConfigModelParam(workflowNodeDtoList);

        // 删除当前工作流所有节点数据
        workflowService.deleteWorkflowAllNodeData(workflowId);

        // 加入所有工作流新节点
        List<WorkflowNodeInput> workflowNodeInputList = new ArrayList<>();
        List<WorkflowNodeOutput> workflowNodeOutputList = new ArrayList<>();
        List<WorkflowNodeCode> workflowNodeCodeList = new ArrayList<>();
        List<WorkflowNodeResource> workflowNodeResourceList = new ArrayList<>();
        List<WorkflowNodeVariable> workflowNodeVariableList = new ArrayList<>();
        // 节点记数
        int count = 0;
        // 算法集合
        Set<Long> algorithmIdSet = new HashSet<>();
        for (WorkflowNodeDto workflowNodeDto : workflowNodeDtoList) {
            // 判断是否添加相同的算法
            if (algorithmIdSet.contains(workflowNodeDto.getAlgorithmId())) {
                log.error("saveWorkflowNode--判断是否添加相同的算法--workflowNodeDto:{}", JSON.toJSONString(workflowNodeDto));
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.ALG_REPEAT_ERROR.getMsg());
            }
            algorithmIdSet.add(workflowNodeDto.getAlgorithmId());
            // 保存工作流节点
            Long workflowNodeId = this.saveWorkflowNode(workflowId, workflowNodeDto, ++count, workflowNodeDtoList.size());
            // 拼装工作流节点输入
            workflowNodeInputList.addAll(this.assemblyNodeInput(workflowNodeId, workflowNodeDto));
            // 拼装工作流节点输出
            workflowNodeOutputList.addAll(this.assemblyNodeOutput(workflowNodeId, workflowNodeDto));
            // 添加新的工作流节点代码
            if (Objects.nonNull(workflowNodeDto.getWorkflowNodeCode())) {
                workflowNodeDto.getWorkflowNodeCode().setWorkflowNodeId(workflowNodeId);
                workflowNodeCodeList.add(workflowNodeDto.getWorkflowNodeCode());
            }
            // 添加新的工作流节点资源
            if (Objects.nonNull(workflowNodeDto.getWorkflowNodeResource())) {
                workflowNodeDto.getWorkflowNodeResource().setWorkflowNodeId(workflowNodeId);
                workflowNodeResourceList.add(workflowNodeDto.getWorkflowNodeResource());
            }
            // 添加新的工作流节点变量
            workflowNodeVariableList.addAll(this.saveWorkflowNodeVariable(workflowNodeId, workflowNodeDto));
        }
        // 保存节点相关数据
        this.saveNodeData(workflowNodeInputList, workflowNodeOutputList, workflowNodeCodeList,
                workflowNodeResourceList, workflowNodeVariableList);
        // 更新工作流节点数量
        workflow.setNodeNumber(workflowNodeDtoList.size());
        workflow.setUpdateTime(new Date());
        workflowService.updateById(workflow);
    }

    /** 校验节点配置输入是否有过期数据 */
    private void checkExpireConfigData(List<WorkflowNodeDto> workflowNodeDtoList){
        Set<String> userAuthDataIdSet = new HashSet<>();
        for (WorkflowNodeDto workflowNodeDto : workflowNodeDtoList) {
            List<WorkflowNodeInput> workflowNodeInputList = workflowNodeDto.getWorkflowNodeInputList();
            if (null == workflowNodeInputList || workflowNodeInputList.size() == 0) {
                continue;
            }
            for (WorkflowNodeInput workflowNodeInput : workflowNodeInputList) {
                userAuthDataIdSet.add(workflowNodeInput.getDataTableId());
            }
        }
        if (userAuthDataIdSet.size() > 0) {
            List<UserMetaData> userMetaDataList = userMetaDataService.getByMetaDataId(userAuthDataIdSet);
            if (null != userMetaDataList  && userMetaDataList.size() < userAuthDataIdSet.size()) {
                log.error("有授权数据已过期，请检查, userAuthDataIdSet:{}", JSON.toJSONString(userAuthDataIdSet));
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.METADATA_USER_DATA_EXPIRE.getMsg());
            }
        }
    }

    /** 校验只有一个算法节点，且该节点需要模型时，是否传入模型参数 */
    private void checkConfigModelParam(List<WorkflowNodeDto> workflowNodeDtoList){
        // 当前工作流只有一个节点，且需要输入模型
        if (workflowNodeDtoList.size() == 1) {
            int inputModel = workflowNodeDtoList.get(0).getAlgorithmDto().getInputModel();
            Long ModelId = workflowNodeDtoList.get(0).getModelId();
            // inputModel: 是否需要输入模型: 0-否，1:是
            boolean flag = SysConstant.INT_1 == inputModel && (null == ModelId || ModelId == 0);
            if (flag) {
                log.error("当前节点需输入模型，请检查, workflowNodeDto:{}", JSON.toJSONString(workflowNodeDtoList.get(0)));
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_NODE_MODEL_NOT_EXIST.getMsg());
            }
        }
    }

    /**
     * 保存工作流节点
     */
    private Long saveWorkflowNode(Long workflowId, WorkflowNodeDto workflowNodeDto, int count, int listSize) {
        WorkflowNode workflowNode = new WorkflowNode();
        workflowNode.setWorkflowId(workflowId);
        workflowNode.setNodeName(workflowNodeDto.getNodeName());
        workflowNode.setAlgorithmId(workflowNodeDto.getAlgorithmId());
        workflowNode.setNodeStep(workflowNodeDto.getNodeStep());
        workflowNode.setModelId(workflowNodeDto.getModelId());
        if (count == listSize) {
            // 将最后一个节点步骤的下一节点步骤字段值置空
            workflowNode.setNextNodeStep(null);
        } else {
            workflowNode.setNextNodeStep(workflowNodeDto.getNodeStep() + 1);
        }
        this.save(workflowNode);
        return workflowNode.getId();
    }

    /**
     * 拼装工作流节点输入
     */
    private List<WorkflowNodeInput> assemblyNodeInput(Long workflowNodeId, WorkflowNodeDto dto) {
        if (null != dto.getWorkflowNodeInputList() && dto.getWorkflowNodeInputList().size() > 0) {
            String[] identityIdArr = new String[dto.getWorkflowNodeInputList().size()];
            //任务里面定义的 (p0 -> pN 方 ...)
            //设置工作流节点id
            for (int i = 0; i < dto.getWorkflowNodeInputList().size(); i++) {
                dto.getWorkflowNodeInputList().get(i).setPartyId("p" + i);
                dto.getWorkflowNodeInputList().get(i).setWorkflowNodeId(workflowNodeId);
                identityIdArr[i] = dto.getWorkflowNodeInputList().get(i).getIdentityId();
            }
            // 校验组织信息
            List<Organization> organizationList = organizationService.getByIdentityIds(identityIdArr);
            if (null == organizationList || organizationList.size() != identityIdArr.length) {
                List<Organization> newOrganizationList = syncOrganization();
                Set<String> identityIdSet = new HashSet<>();
                newOrganizationList.forEach(o -> identityIdSet.add(o.getIdentityId()));
                for (String identity : identityIdArr) {
                    if (!identityIdSet.contains(identity)) {
                        log.error("AssemblyNodeInput->前端输入的机构信息identity:{}未找到", identity);
                        throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.ORGANIZATION_NOT_EXIST.getMsg());
                    }
                }
            }
        }
        return dto.getWorkflowNodeInputList() == null ? new ArrayList<>() : dto.getWorkflowNodeInputList();
    }

    /**
     * 拼装工作流节点输出数据
     */
    private List<WorkflowNodeOutput> assemblyNodeOutput(Long workflowNodeId, WorkflowNodeDto dto) {
        List<WorkflowNodeOutput> outputList = dto.getWorkflowNodeOutputList();
        if (null != outputList && outputList.size() > 0) {
            // 任务里面定义的 (p0 -> pN 方 ...)
            for (int i = 0; i < outputList.size(); i++) {
                outputList.get(i).setPartyId("q" + i);
                outputList.get(i).setWorkflowNodeId(workflowNodeId);
            }
        }
        return outputList == null ? new ArrayList<>() : outputList;
    }

    /**
     * 保存工作流节点输入变量
     */
    private List<WorkflowNodeVariable> saveWorkflowNodeVariable(Long workflowNodeId, WorkflowNodeDto workflowNodeDto) {
        List<WorkflowNodeVariable> workflowNodeVariableList = workflowNodeDto.getWorkflowNodeVariableList();
        if (null == workflowNodeVariableList || workflowNodeVariableList.size() == 0) {
            return new ArrayList<>();
        }
        workflowNodeVariableList.forEach(workflowNodeVariable -> workflowNodeVariable.setWorkflowNodeId(workflowNodeId));
        return workflowNodeVariableList;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void clearWorkflowNode(Long workflowId) {
        Workflow workflow = workflowService.getById(workflowId);
        if (Objects.isNull(workflow)) {
            return;
        }
        // 校验是否有编辑权限
        checkEditPermission(workflow.getProjectId());
        List<WorkflowNode> workflowNodeList = this.getWorkflowNodeList(workflowId);
        if (null == workflowNodeList || workflowNodeList.size() == 0) {
            return;
        }
        // 校验工作流状态是否运行中
        boolean isExistNodeRunning = workflowNodeList.stream().anyMatch(workflowNode -> workflowNode.getRunStatus() == WorkflowRunStatusEnum.RUNNING.getValue());
        if(workflow.getRunStatus() == WorkflowRunStatusEnum.RUNNING.getValue() || isExistNodeRunning){
            log.error("Workflow runStatus is running or workflow node exist runStatus is running,can not clear, workflowId:{}", workflowId);
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_NOT_CLEAR.getMsg());
        }
        // 物理删除当前工作流所有节点数据
        workflowService.deleteWorkflowAllNodeData(workflowId);
    }

    @Override
    public WorkflowNode getByWorkflowIdAndStep(Long workflowId, Integer nodeStep) {
        LambdaQueryWrapper<WorkflowNode> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNode::getWorkflowId, workflowId);
        wrapper.eq(WorkflowNode::getNodeStep, nodeStep);
        wrapper.eq(WorkflowNode::getStatus, StatusEnum.VALID.getValue());
        WorkflowNode workflowNode = this.getOne(wrapper);
        if (workflowNode == null) {
            log.error("workflow node not found by workflowId:{},nodeStep:{}", workflowId, nodeStep);
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.WORKFLOW_NODE_NOT_EXIST.getMsg());
        }
        return workflowNode;
    }

    @Override
    public List<WorkflowNode> getWorkflowNodeList(Long workflowId) {
        LambdaQueryWrapper<WorkflowNode> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNode::getWorkflowId, workflowId);
        wrapper.eq(WorkflowNode::getStatus, StatusEnum.VALID.getValue());
        // 所有节点正序排序
        wrapper.orderByAsc(WorkflowNode::getNodeStep);
        return this.list(wrapper);
    }

    @Override
    public void saveCopyWorkflowNodeTemp(Long newWorkflowId, List<WorkflowNode> oldNodeList) {
        oldNodeList.forEach(oldNode -> this.copyWorkflowNode(oldNode, newWorkflowId));
    }

    /** 复制工作流节点 */
    private WorkflowNode copyWorkflowNode(WorkflowNode oldNode, Long newWorkflowId) {
        WorkflowNode newNode = new WorkflowNode();
        newNode.setWorkflowId(newWorkflowId);
        newNode.setAlgorithmId(oldNode.getAlgorithmId());
        newNode.setNodeName(oldNode.getNodeName());
        newNode.setNodeStep(oldNode.getNodeStep());
        newNode.setNextNodeStep(oldNode.getNextNodeStep());
        this.save(newNode);
        return newNode;
    }

    @Override
    public void saveCopyWorkflowNode(Long newWorkflowId, List<WorkflowNode> oldNodeList) {
        if (null == oldNodeList || oldNodeList.size() == 0) {
            return;
        }
        List<WorkflowNodeInput> newNodeInputList = new ArrayList<>();
        List<WorkflowNodeOutput> newNodeOutputList = new ArrayList<>();
        List<WorkflowNodeCode> newNodeCodeList = new ArrayList<>();
        List<WorkflowNodeResource> newNodeResourceList = new ArrayList<>();
        List<WorkflowNodeVariable> newNodeVariableList = new ArrayList<>();
        oldNodeList.forEach(oldNode -> {
            // 复制节点
            WorkflowNode newNode = this.copyWorkflowNode(oldNode, newWorkflowId);
            // 复制节点输入数据
            List<WorkflowNodeInput> workflowNodeInputList = workflowNodeInputService.copyWorkflowNodeInput(newNode.getId(), oldNode.getId());
            newNodeInputList.addAll(workflowNodeInputList);
            // 复制节点输出数据
            List<WorkflowNodeOutput> workflowNodeOutputList = workflowNodeOutputService.copyWorkflowNodeOutput(newNode.getId(), oldNode.getId());
            newNodeOutputList.addAll(workflowNodeOutputList);
            // 复制节点算法代码
            WorkflowNodeCode workflowNodeCode = workflowNodeCodeService.copyWorkflowNodeCode(newNode.getId(), oldNode.getId());
            if (Objects.nonNull(workflowNodeCode)) {
                newNodeCodeList.add(workflowNodeCode);
            }
            // 复制节点环境资源
            WorkflowNodeResource workflowNodeResource = workflowNodeResourceService.copyWorkflowNodeResource(newNode.getId(), oldNode.getId());
            if (Objects.nonNull(workflowNodeResource)) {
                newNodeResourceList.add(workflowNodeResource);
            }
            // 复制节点变量
            List<WorkflowNodeVariable> workflowNodeVariableList = workflowNodeVariableService.copyWorkflowNodeVariable(newNode.getId(), oldNode.getId());
            if (Objects.nonNull(workflowNodeResource)) {
                newNodeVariableList.addAll(workflowNodeVariableList);
            }
        });
        // 保存节点相关数据
        this.saveNodeData(newNodeInputList, newNodeOutputList, newNodeCodeList,
                newNodeResourceList, newNodeVariableList);
    }

    /**
     * 保存节点相关数据
     */
    private void saveNodeData(List<WorkflowNodeInput> newNodeInputList,
                              List<WorkflowNodeOutput> newNodeOutputList,
                              List<WorkflowNodeCode> newNodeCodeList,
                              List<WorkflowNodeResource> newNodeResourceList,
                              List<WorkflowNodeVariable> newNodeVariableList) {
        // 保存节点输入数据
        if (newNodeInputList.size() > 0) {
            workflowNodeInputService.batchInsert(newNodeInputList);
        }
        // 保存节点输出数据
        if (newNodeOutputList.size() > 0) {
            workflowNodeOutputService.batchInsert(newNodeOutputList);
        }
        // 保存节点算法代码
        if (newNodeCodeList.size() > 0) {
            workflowNodeCodeService.batchInsert(newNodeCodeList);
        }
        // 保存节点环境资源
        if (newNodeResourceList.size() > 0) {
            workflowNodeResourceService.batchInsert(newNodeResourceList);
        }
        // 保存节点变量
        if (newNodeVariableList.size() > 0) {
            workflowNodeVariableService.batchInsert(newNodeVariableList);
        }
    }

    @Override
    public WorkflowNode getRunningNodeByWorkflowId(Long workflowId) {
        LambdaQueryWrapper<WorkflowNode> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNode::getWorkflowId, workflowId);
        wrapper.eq(WorkflowNode::getRunStatus, WorkflowRunStatusEnum.RUNNING.getValue());
        wrapper.eq(WorkflowNode::getStatus, StatusEnum.VALID.getValue());
        return this.getOne(wrapper);
    }

    @Override
    public void updateRunStatusByWorkflowId(Long workflowId, Byte oldRunStatus, Byte newRunStatus) {
        LambdaUpdateWrapper<WorkflowNode> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(WorkflowNode::getRunStatus, newRunStatus);
        updateWrapper.eq(WorkflowNode::getRunStatus, oldRunStatus);
        updateWrapper.eq(WorkflowNode::getWorkflowId, workflowId);
        updateWrapper.eq(WorkflowNode::getStatus, StatusEnum.VALID.getValue());
        this.update(updateWrapper);
    }

    @Override
    public List<WorkflowNode> getRunningNode(int beforeHour) {
        LambdaQueryWrapper<WorkflowNode> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNode::getRunStatus, WorkflowRunStatusEnum.RUNNING.getValue());
        wrapper.eq(WorkflowNode::getStatus, StatusEnum.VALID.getValue());
        wrapper.ge(WorkflowNode::getUpdateTime, DateUtil.offsetHour(new Date(), beforeHour));
        return this.list(wrapper);
    }

    @Override
    public void updateRunStatus(Object[] ids, Byte runStatus) {
        LambdaUpdateWrapper<WorkflowNode> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(WorkflowNode::getRunStatus, runStatus);
        updateWrapper.set(WorkflowNode::getUpdateTime, now());
        updateWrapper.in(WorkflowNode::getId, ids);
        this.update(updateWrapper);
    }

    /**
     * 校验是否有编辑权限
     */
    private void checkEditPermission(Long projectId) {
        Byte role = projectService.getRoleByProjectId(projectId);
        if (null == role || ProjectMemberRoleEnum.VIEW.getRoleId() == role) {
            log.error("Current not permission to edit current project");
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_NOT_PERMISSION_ERROR.getMsg());
        }
    }

    /**
     * 同步组织信息
     *
     * @return 组织信息列表
     */
    private List<Organization> syncOrganization() {
        List<Organization> organizationList = new ArrayList<>();
        List<NodeIdentityDto> nodeIdentityDtoList = grpcAuthService.getIdentityList();
        if (null != nodeIdentityDtoList && nodeIdentityDtoList.size() > 0) {

            Organization org;
            for (NodeIdentityDto nodeIdentityDto : nodeIdentityDtoList) {
                org = new Organization();
                org.setNodeName(nodeIdentityDto.getNodeName());
                org.setNodeId(nodeIdentityDto.getNodeId());
                org.setIdentityId(nodeIdentityDto.getIdentityId());
                organizationList.add(org);
            }
            organizationService.batchInsert(organizationList);
        }
        return organizationList;
    }

}
