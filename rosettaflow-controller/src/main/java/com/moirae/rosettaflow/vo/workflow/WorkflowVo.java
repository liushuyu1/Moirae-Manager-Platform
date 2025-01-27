package com.moirae.rosettaflow.vo.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moirae.rosettaflow.common.constants.SysConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 工作流列表响应对象
 * @author hudenian
 * @date 2021/8/30
 */
@Data
@ApiModel(value = "工作流列表响应对象")
public class WorkflowVo {

    @ApiModelProperty(value = "工作流ID")
    private Long id;

    @ApiModelProperty(value = "工作流名称")
    private String workflowName;

    @ApiModelProperty(value = "工作流描述")
    private String workflowDesc;

    @ApiModelProperty(value = "工作流运行状态(运行状态:0-未运行,1-运行中,2-运行成功，3-运行失败)")
    private Byte runStatus;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = SysConstant.DEFAULT_TIME_PATTERN, timezone = SysConstant.DEFAULT_TIMEZONE)
    private Date createTime;

    @ApiModelProperty(value = "创建者")
    private String userName;

}
