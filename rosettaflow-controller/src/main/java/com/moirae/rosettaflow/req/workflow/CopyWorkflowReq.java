package com.moirae.rosettaflow.req.workflow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @author hudenian
 * @date 2021/8/27
 * @description 复制工作流请求对象
 */
@Data
@ApiModel
public class CopyWorkflowReq {

    @ApiModelProperty(value = "原工作流ID", required = true)
    @NotNull(message = "{workflow.id.notNull}")
    @Positive(message = "{workflow.id.positive}")
    private Long originId;

    @ApiModelProperty(value = "工作流名称", required = true)
    @NotBlank(message = "{workflow.name.notBlank}")
    @Length(max = 30, message = "{workflow.name.Length}")
    private String workflowName;

    @ApiModelProperty(value = "工作流描述", required = true)
    @Length(max = 50, message = "{workflow.desc.Length}}")
    private String workflowDesc;

}
