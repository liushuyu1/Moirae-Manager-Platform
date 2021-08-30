package com.platon.rosettaflow.req.workflow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author hudenian
 * @date 2021/8/27
 * @description 添加工作流请求对象
 */
@Data
@ApiModel
public class AddWorkflowReq {

    @ApiModelProperty(value = "工作流名称", required = true)
    @NotBlank(message = "工作流名称不能为空")
    @Length(max = 30, message = "工作流名称不能大于30个字符")
    private String workflowName;

    @ApiModelProperty(value = "工作流描述", required = true)
    @NotBlank(message = "工作流描述不能为空")
    @Length(max = 50, message = "工作流描述不能大于50个字符")
    private String workflowDesc;

}