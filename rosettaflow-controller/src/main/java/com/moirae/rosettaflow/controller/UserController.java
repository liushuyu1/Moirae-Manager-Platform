package com.moirae.rosettaflow.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.moirae.rosettaflow.common.constants.SysConstant;
import com.moirae.rosettaflow.common.enums.ErrorMsg;
import com.moirae.rosettaflow.common.enums.RespCodeEnum;
import com.moirae.rosettaflow.common.exception.BusinessException;
import com.moirae.rosettaflow.common.utils.AddressChangeUtils;
import com.moirae.rosettaflow.dto.UserDto;
import com.moirae.rosettaflow.mapper.domain.Organization;
import com.moirae.rosettaflow.mapper.domain.User;
import com.moirae.rosettaflow.req.user.LoginInReq;
import com.moirae.rosettaflow.req.user.UpdateNickReq;
import com.moirae.rosettaflow.req.user.UserDetailsReq;
import com.moirae.rosettaflow.service.IOrganizationService;
import com.moirae.rosettaflow.service.IUserOrgMaintainService;
import com.moirae.rosettaflow.service.IUserService;
import com.moirae.rosettaflow.utils.WalletSignUtils;
import com.moirae.rosettaflow.vo.ResponseVo;
import com.moirae.rosettaflow.vo.user.NonceVo;
import com.moirae.rosettaflow.vo.user.UserNicknameVo;
import com.moirae.rosettaflow.vo.user.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author admin
 * @date 2021/8/17
 */
@Slf4j
@RestController
@Api(tags = "用户管理关接口")
@RequestMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IOrganizationService organizationService;

    @Resource
    private IUserOrgMaintainService userOrgMaintainService;

    @GetMapping("getLoginNonce/{address}")
    @ApiOperation(value = "获取登录Nonce", notes = "获取登录Nonce")
    public ResponseVo<NonceVo> getLoginNonce(@ApiParam(value = "用户钱包地址", required = true) @PathVariable String address) {
        return ResponseVo.createSuccess(new NonceVo(userService.getLoginNonce(AddressChangeUtils.convert0xAddress(address))));
    }

    @PostMapping("login")
    @ApiOperation(value = "用户登录", notes = "用户登录")
    public ResponseVo<UserVo> login(@RequestBody @Validated LoginInReq loginInReq) {
        log.info("用户登录钱包地址:{},发起登录操作", loginInReq.getAddress());
        userService.checkNonceValidity(loginInReq.getSignMessage(), loginInReq.getAddress());
        boolean flg;
        try {
            String signMessage = StrUtil.replace(loginInReq.getSignMessage(), "\\\"", "\"");
            flg = WalletSignUtils.verifyTypedDataV4(signMessage, loginInReq.getSign(), loginInReq.getHrpAddress());
        } catch (Exception e) {
            log.error("User login signature error,error msg:{}", e.getMessage(), e);
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_SIGN_ERROR.getMsg());
        }
        if (!flg) {
            log.error("User login signature error");
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_SIGN_ERROR.getMsg());
        }
        UserDto userDto = userService.generatorToken(loginInReq.getAddress(), loginInReq.getHrpAddress());
        // 随机取第一个组织进行连接连接组织
        this.randomConnect(userDto);
        log.info("用户登录钱包地址:{},登录登录成功！", loginInReq.getAddress());
        return ResponseVo.createSuccess(BeanUtil.copyProperties(userDto, UserVo.class));
    }

    /** 随机取第一个组织进行连接连接组织 */
    private void randomConnect(UserDto userDto) {
        try {
            List<Organization> organizationList = organizationService.getAllPublicByIdentity();
            if (CollectionUtil.isEmpty(organizationList)) {
                log.error("用户登录时没有获取到公共组织-randomConnect, organizationList:{}", organizationList);
                return;
            }
            // 随机取第一个组织
            Organization organization = organizationList.get(0);
            // 检验保存连接组织信息
            userOrgMaintainService.checkConnectIdentity(userDto, organization.getIdentityId(), organization.getIdentityIp(), organization.getIdentityPort());
        } catch (Exception e) {
            log.error("登录时，检验保存连接组织信息失败-checkConnectIdentity, 错误信息:{}", e.getMessage());
        }
    }

    @PostMapping("logout")
    @ApiOperation(value = "用户登出", notes = "用户登出")
    public ResponseVo<?> logout(HttpServletResponse httpServletResponse) {
        userService.logout();
        httpServletResponse.setHeader(SysConstant.HEADER_TOKEN_KEY, "");
        return ResponseVo.createSuccess();
    }

    @PostMapping("updateNickName")
    @ApiOperation(value = "修改昵称", notes = "修改昵称")
    public ResponseVo<?> updateNickName(@RequestBody @Valid UpdateNickReq updateNickReq) {
        userService.updateNickName(updateNickReq.get0xAddress(), updateNickReq.getNickName());
        return ResponseVo.create(RespCodeEnum.SUCCESS);
    }

    @PostMapping("queryUserDetails")
    @ApiOperation(value = "查询用户详情", notes = "查询用户详情")
    public ResponseVo<UserVo> queryUserDetails(@RequestBody @Valid UserDetailsReq userDetailsReq) {
        User user = userService.getByAddress(userDetailsReq.getAddress());
        return ResponseVo.createSuccess(BeanUtil.toBean(user, UserVo.class));
    }

    @GetMapping("queryAllUserNickname")
    @ApiOperation(value = "查询所有用户昵称", notes = "查询所有用户昵称")
    public ResponseVo<List<UserNicknameVo>> queryAllUserNickname() {
        List<User> list = userService.queryAllUserNickName();
        return ResponseVo.createSuccess(BeanUtil.copyToList(list, UserNicknameVo.class));
    }

}
