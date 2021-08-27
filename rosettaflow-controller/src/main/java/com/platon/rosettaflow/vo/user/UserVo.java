package com.platon.rosettaflow.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hudenian
 * @date 2021/8/16
 * @description 用户信息返回对象
 */
@Data
@ApiModel("登录返回参数")
public class UserVo {

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("用户钱包地址")
    private String address;

    @ApiModelProperty("用户类型")
    private Byte userType;

    @ApiModelProperty("用户登录token")
    private String token;

}
