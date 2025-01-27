package com.moirae.rosettaflow.common.enums;

/**
 * @author hudenian
 * @date 2021/8/16
 * @description 工作流节点运行状态
 */
public enum WorkflowRunStatusEnum {
    /**
     * 未运行
     */
    UN_RUN((byte) 0),
    /**
     * 运行中
     */
    RUNNING((byte) 1),
    /**
     * 运行成功
     */
    RUN_SUCCESS((byte) 2),
    /**
     * 运行失败
     */
    RUN_FAIL((byte) 3);

    private final byte value;

    WorkflowRunStatusEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

}
