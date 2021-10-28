package com.moirae.rosettaflow.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moirae.rosettaflow.common.enums.*;
import com.moirae.rosettaflow.common.exception.BusinessException;
import com.moirae.rosettaflow.common.utils.AddressChangeUtils;
import com.moirae.rosettaflow.dto.MetaDataDto;
import com.moirae.rosettaflow.dto.UserDto;
import com.moirae.rosettaflow.dto.UserMetaDataDto;
import com.moirae.rosettaflow.grpc.constant.GrpcConstant;
import com.moirae.rosettaflow.grpc.identity.dto.NodeIdentityDto;
import com.moirae.rosettaflow.grpc.metadata.req.dto.ApplyMetaDataAuthorityRequestDto;
import com.moirae.rosettaflow.grpc.metadata.req.dto.MetaDataAuthorityDto;
import com.moirae.rosettaflow.grpc.metadata.req.dto.MetaDataUsageRuleDto;
import com.moirae.rosettaflow.grpc.metadata.resp.dto.ApplyMetaDataAuthorityResponseDto;
import com.moirae.rosettaflow.grpc.service.GrpcAuthService;
import com.moirae.rosettaflow.mapper.UserMetaDataMapper;
import com.moirae.rosettaflow.mapper.domain.MetaData;
import com.moirae.rosettaflow.mapper.domain.UserMetaData;
import com.moirae.rosettaflow.service.IMetaDataService;
import com.moirae.rosettaflow.service.IUserMetaDataService;
import com.moirae.rosettaflow.service.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author hudenian
 * @date 2021/8/24
 * @description 功能描述
 */
@Slf4j
@Service
public class UserMetaDataServiceImpl extends ServiceImpl<UserMetaDataMapper, UserMetaData> implements IUserMetaDataService {

    @Resource
    private IMetaDataService metaDataService;

    @Resource
    private GrpcAuthService grpcAuthService;

    @Override
    public void truncate() {
        this.baseMapper.truncate();
    }

    @Override
    public IPage<UserMetaDataDto> list(Long current, Long size, String dataName) {
        Page<UserMetaData> page = new Page<>(current, size);
        if (Objects.isNull(UserContext.get()) || null == UserContext.get().getAddress()) {
            log.error(ErrorMsg.USER_UN_LOGIN.getMsg());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_UN_LOGIN.getMsg());
        }
        return this.baseMapper.listByOwner(page, UserContext.get().getAddress(), dataName);
    }

    @Override
    public void auth(UserMetaDataDto userMetaDataDto) {
        MetaData metaData = metaDataService.getById(userMetaDataDto.getId());
        if (null == metaData) {
            log.error(ErrorMsg.METADATA_NOT_EXIST.getMsg());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.METADATA_NOT_EXIST.getMsg());
        }
        if (userMetaDataDto.getAuthType() == MetaDataUsageEnum.TIMES.getValue()) {
            if (null == userMetaDataDto.getAuthValue() || userMetaDataDto.getAuthValue() < 1) {
                log.error(ErrorMsg.METADATA_AUTH_TIMES_ERROR.getMsg());
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.METADATA_AUTH_TIMES_ERROR.getMsg());
            }
        } else {
            if (null == userMetaDataDto.getAuthEndTime() || null == userMetaDataDto.getAuthBeginTime() ||
                    DateUtil.compare(userMetaDataDto.getAuthEndTime(), new Date()) < 0 || userMetaDataDto.getAuthEndTime().before(userMetaDataDto.getAuthBeginTime())) {
                log.error(ErrorMsg.METADATA_AUTH_TIME_ERROR.getMsg());
                throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.METADATA_AUTH_TIME_ERROR.getMsg());
            }
        }

        ApplyMetaDataAuthorityRequestDto applyDto = new ApplyMetaDataAuthorityRequestDto();
        applyDto.setUser(userMetaDataDto.getAddress());
        applyDto.setUserType(UserTypeEnum.checkUserType(userMetaDataDto.getAddress()));

        MetaDataAuthorityDto metaDataAuthorityDto = new MetaDataAuthorityDto();
        //元数据所属组织
        NodeIdentityDto owner = new NodeIdentityDto();
        owner.setNodeName(metaData.getIdentityName());
        owner.setNodeId(metaData.getNodeId());
        owner.setIdentityId(metaData.getIdentityId());
        metaDataAuthorityDto.setOwner(owner);

        //元数据id
        metaDataAuthorityDto.setMetaDataId(metaData.getMetaDataId());

        //元数据怎么使用
        MetaDataUsageRuleDto metaDataUsageDto = new MetaDataUsageRuleDto();
        metaDataUsageDto.setUseType((int) userMetaDataDto.getAuthType());
        if (userMetaDataDto.getAuthType() == MetaDataUsageEnum.PERIOD.getValue()) {
            metaDataUsageDto.setStartAt(userMetaDataDto.getAuthBeginTime().getTime());
            metaDataUsageDto.setEndAt(userMetaDataDto.getAuthEndTime().getTime());
        }
        metaDataUsageDto.setTimes(userMetaDataDto.getAuthValue());
        metaDataAuthorityDto.setMetaDataUsageDto(metaDataUsageDto);

        applyDto.setAuth(metaDataAuthorityDto);
        applyDto.setSign(userMetaDataDto.getSign());

        ApplyMetaDataAuthorityResponseDto responseDto = grpcAuthService.applyMetaDataAuthority(applyDto);
        if (responseDto.getStatus() != GrpcConstant.GRPC_SUCCESS_CODE) {
            log.info("元数据授权申请,net处理失败，失败原因：{}", responseDto.getMsg());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, responseDto.getMsg());
        }
        log.info("元数据授权申请id为：{}", responseDto.getMetaDataAuthId());
    }

    @Override
    public List<UserMetaDataDto> getAllAuthOrganization() {
        UserDto userDto = UserContext.get();
        if (Objects.isNull(userDto)) {
            log.error(ErrorMsg.USER_UN_LOGIN.getMsg());
            throw new BusinessException(RespCodeEnum.BIZ_FAILED, ErrorMsg.USER_UN_LOGIN.getMsg());
        }
        String address = userDto.getAddress();
        if (!StrUtil.startWith(userDto.getAddress(), AddressChangeUtils.HRP_ETH)) {
            address = AddressChangeUtils.convert0xAddress(address);
        }
        return this.baseMapper.getUserMetaDataByAddress(address);
    }

    @Override
    public List<MetaDataDto> getAllAuthTables(String identityId) {
        return metaDataService.getAllAuthTables(identityId);
    }

    @Override
    public UserMetaData getCurrentUserMetaDataByMetaDataId(String metaDataId) {
        //用户没有登录不查询
        if (Objects.isNull(UserContext.get()) || null == UserContext.get().getAddress()) {
            return null;
        }
        LambdaQueryWrapper<UserMetaData> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserMetaData::getMetaDataId, metaDataId);
        wrapper.eq(UserMetaData::getAddress, UserContext.get().getAddress());
        wrapper.eq(UserMetaData::getAuthMetadataState, UserMetaDataAuthorithStateEnum.RELEASED.getValue());
        return this.getOne(wrapper);
    }

    @Override
    public List<UserMetaData> getCurrentUserMetaDataByMetaDataIdArr(Object[] metaDataIdArr) {
        LambdaQueryWrapper<UserMetaData> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UserMetaData::getMetaDataId, metaDataIdArr);
        wrapper.eq(UserMetaData::getAddress, UserContext.get().getAddress());
        return this.list(wrapper);
    }

    @Override
    public int batchInsert(List<UserMetaData> userMetaDataList) {
        return this.baseMapper.batchInsert(userMetaDataList);
    }
}