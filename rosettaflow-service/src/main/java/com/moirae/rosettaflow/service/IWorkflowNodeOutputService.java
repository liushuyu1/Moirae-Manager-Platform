package com.moirae.rosettaflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moirae.rosettaflow.mapper.domain.WorkflowNodeOutput;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/18
 * @description 工作流节点输出接口
 */
public interface IWorkflowNodeOutputService extends IService<WorkflowNodeOutput> {
    /**
     * 根据工作流节点id获取工作流节点输出列表
     *
     * @param workflowNodeId 工作流节点id
     * @return 工作流节点输出列表
     */
    List<WorkflowNodeOutput> getByWorkflowNodeId(Long workflowNodeId);

    /**
     * 物理批量删除工作流节点输出数据，根据节点id
     * @param workflowNodeIdList 工作流节点id列表
     */
    void deleteByWorkflowNodeId(List<Long> workflowNodeIdList);

    /**
     * 复制工作流节点输出数据
     *
     * @param newNodeId 新节点id
     * @param oldNodeId 旧节点id
     * @return 工作流节点输出列表
     */
    List<WorkflowNodeOutput> copyWorkflowNodeOutput(Long newNodeId, Long oldNodeId);

    /**
     * 批量保存节点输出
     *
     * @param workflowNodeOutputList 节点输出列表
     */
    void batchInsert(List<WorkflowNodeOutput> workflowNodeOutputList);
}