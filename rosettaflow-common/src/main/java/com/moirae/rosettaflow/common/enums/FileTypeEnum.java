package com.moirae.rosettaflow.common.enums;

/**
 * @author hudenian
 * @date 2021/9/10
 * @description 文件类型枚举
 */
public enum FileTypeEnum {

    /**
     * 未知
     */
    FILETYPE_UNKONW(0),
    /**
     * CSV类型
     */
    FILETYPE_CSV(1);


    private final int value;

    FileTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
