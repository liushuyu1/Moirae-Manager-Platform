package com.moirae.rosettaflow.req.organization;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 删除ip及port与组织绑定关系请求对象
 *
 * @author hudenian
 * @date 2021/12/15
 */
@Data
@ApiModel(value = "删除ip及port与组织绑定关系请求")
public class DelIpPortBindReq {

    @ApiModelProperty(value = "用户组织连接绑定关系表ID", required = true)
    @NotNull(message = "{job.id.notNull}")
    @Positive(message = "{job.id.positive}")
    private Long id;
}
