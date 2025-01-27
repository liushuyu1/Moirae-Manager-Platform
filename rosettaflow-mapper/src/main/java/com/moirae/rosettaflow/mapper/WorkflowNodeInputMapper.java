package com.moirae.rosettaflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moirae.rosettaflow.dto.NodeMetaDataDto;
import com.moirae.rosettaflow.mapper.domain.WorkflowNodeInput;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author admin
 */
public interface WorkflowNodeInputMapper extends BaseMapper<WorkflowNodeInput> {

    /**
     * 根据工作流节点id获取相关元数据信息
     *
     * @param workflowNodeId 工作流节点id
     * @return List
     */
    List<NodeMetaDataDto> getMetaDataByWorkflowNodeId(@Param("workflowNodeId") Long workflowNodeId);

    /**
     * 批量保存节点输入
     *
     * @param workflowNodeInputList 节点输入列表
     * @return 保存记录数
     */
    int batchInsert(@Param("workflowNodeInputList") List<WorkflowNodeInput> workflowNodeInputList);

}