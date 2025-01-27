package com.moirae.rosettaflow.req.job;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @author juzix
 */
@Data
@ApiModel(value = "删除子作业请求参数")
public class DeleteBatchSubJobReq {
    @ApiModelProperty(value = "子作业id集合", required = true)
    private List<Long> subJobIds;
}
