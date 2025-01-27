package com.moirae.rosettaflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moirae.rosettaflow.dto.NodeMetaDataDto;
import com.moirae.rosettaflow.mapper.domain.WorkflowNodeInput;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/18
 * @description 工作流节点输入服务接口
 */
public interface IWorkflowNodeInputService extends IService<WorkflowNodeInput> {

    /**
     * 根据工作流节点id获取工作流节点输入列表
     *
     * @param workflowNodeId 工作流节点id
     * @return 工作流节点输入列表
     */
    List<WorkflowNodeInput> getByWorkflowNodeId(Long workflowNodeId);

    /**
     * 物理批量删除工作流节点输入数据，根据节点id
     *
     * @param workflowNodeIdList 工作流节点id列表
     */
    void deleteByWorkflowNodeId(List<Long> workflowNodeIdList);

    /**
     * 逻辑删除工作流节点输入, 根据工作流节点id
     *
     * @param workflowNodeId 工作流节点id
     */
    void deleteLogicByWorkflowNodeId(Long workflowNodeId);

    /**
     * 复制工作流节点输入数据
     *
     * @param newNodeId 新工作流节点id
     * @param oldNodeId 旧工作流节点id
     * @return WorkflowNodeInput
     */
    List<WorkflowNodeInput> copyWorkflowNodeInput(Long newNodeId, Long oldNodeId);

    /**
     * 批量保存节点输入
     *
     * @param workflowNodeInputList 节点输入列表
     */
    void batchInsert(List<WorkflowNodeInput> workflowNodeInputList);

    /**
     * 根据工作流节点id查询相关元数据信息
     *
     * @param workflowNodeId 工作流节点id
     * @return 工元数据信息列表
     */
    List<NodeMetaDataDto> getMetaDataByWorkflowNodeId(Long workflowNodeId);
}
