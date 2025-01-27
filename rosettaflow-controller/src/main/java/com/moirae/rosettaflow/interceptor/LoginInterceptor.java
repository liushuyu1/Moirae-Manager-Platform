package com.moirae.rosettaflow.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.moirae.rosettaflow.common.constants.SysConstant;
import com.moirae.rosettaflow.common.enums.RespCodeEnum;
import com.moirae.rosettaflow.common.utils.LanguageContext;
import com.moirae.rosettaflow.dto.UserDto;
import com.moirae.rosettaflow.service.ITokenService;
import com.moirae.rosettaflow.service.IUserService;
import com.moirae.rosettaflow.service.utils.UserContext;
import com.moirae.rosettaflow.utils.IpUtils;
import com.moirae.rosettaflow.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author admin
 * @date 2021/8/17
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "application/json;charset=utf-8";

    @Resource
    private ITokenService tokenService;

    @Resource
    private IUserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        boolean needToken = true;
        for (int i = 0; i < SysConstant.LOGIN_URIS.length; i++) {
            if (request.getRequestURI().contains(SysConstant.LOGIN_URIS[i])) {
                needToken = false;
                break;
            }
        }

        addRequestId();

        LanguageContext.set(request.getHeader("Accept-Language") == null ? SysConstant.ZH_CN : request.getHeader("Accept-Language"));

        log.info("Request Info: [Method = {}], [URI = {}], [Client-IP = {}], [userAgent = {}]", request.getMethod(),
                request.getRequestURI(), IpUtils.getIpAddr(request), request.getHeader("user-agent"));

        String token = request.getHeader(SysConstant.HEADER_TOKEN_KEY);
        UserDto userDto;

        if (StrUtil.isNotEmpty(token)) {
            userDto = tokenService.getUserByToken(token);
            if (null != userDto) {
                if (null == userService.getByAddress(userDto.getAddress())) {
                    tokenService.removeToken(token);
                    log.error("user not exist: {}", userDto.getAddress());
                    printResponse(response, RespCodeEnum.USER_NOT_EXIST);
                    return false;
                }
                UserContext.set(userDto);
                tokenService.refreshToken(token);
            } else {
                if (needToken) {
                    log.error("Invalid token: {}", token);
                    printResponse(response, RespCodeEnum.TOKEN_INVALID);
                    return false;
                }
            }
        } else {
            if (needToken) {
                printResponse(response, RespCodeEnum.UN_LOGIN);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        removeRequestId();
        UserContext.remove();
        LanguageContext.remove();
    }

    void addRequestId() {
        MDC.put("requestId", UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
    }

    void removeRequestId() {
        MDC.clear();
    }

    void printResponse(HttpServletResponse response, RespCodeEnum respCodeEnum) throws IOException {
        response.setCharacterEncoding(ENCODING);
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(JSON.toJSONString(ResponseVo.create(respCodeEnum)));
    }

}
