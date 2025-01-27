package com.moirae.rosettaflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moirae.rosettaflow.mapper.domain.WorkflowNodeOutput;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author admin
 */
public interface WorkflowNodeOutputMapper extends BaseMapper<WorkflowNodeOutput> {

    /**
     * 查询节点输出列表
     *
     * @param workflowNodeId 工作流节点id
     * @return 节点输出列表
     */
    List<WorkflowNodeOutput> getByWorkflowNodeId(Long workflowNodeId);

    /**
     * 根据任务id获取输入放的组织id
     *
     * @param taskId 任务id
     * @return identityId 组织id
     */
    String getOutputIdentityIdByTaskId(@Param("taskId") String taskId);

    /**
     * 根据工作流id 及节点编号获取任务结果输出组织的IdentityId
     *
     * @param workflowNodeId 工作流id
     * @param nodeStep       节点编号
     * @return identityId
     */
    String getOutputIdentityIdByWorkFlowIdAndStep(@Param("workflowNodeId") Long workflowNodeId, @Param("nodeStep") Long nodeStep);

    /**
     * 批量保存节点输出
     *
     * @param workflowNodeOutputList 节点输出列表
     * @return 保存记录数
     */
    int batchInsert(@Param("workflowNodeOutputList") List<WorkflowNodeOutput> workflowNodeOutputList);
}
