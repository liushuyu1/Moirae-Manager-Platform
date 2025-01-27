package com.moirae.rosettaflow.dto;

import com.moirae.rosettaflow.mapper.domain.TaskResult;
import com.moirae.rosettaflow.mapper.domain.Workflow;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/17
 * @description 功能描述
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowDto extends Workflow {

    /**
     * 创建者
     */
    private String userName;

    /**
     * 起始节点
     */
    private Integer startNode;
    /**
     * 截止节点
     */
    private Integer endNode;

    /**
     * 是否是job任务
     */
    private boolean jobFlg = false;
    /**
     * 子作业id(主键)
     */
    private Long subJobId;

    /**
     * 工作流节点列表
     */
    private List<WorkflowNodeDto> workflowNodeDtoList;

    /**
     * 发起任务的账户地址
     */
    private String address;

    /**
     * 发起任务的账户的签名
     */
    private String sign;
    /**
     * 工作流正在执行的taskId
     */
    private String taskId;
    /**
     * 工作流前一个节点执行的taskId
     */
    private String preTaskId;
    /**
     * 上一个节点运行结果
     */
    private TaskResult preTaskResult;

}
