package com.platon.rosettaflow.task;

import cn.hutool.core.date.DateUtil;
import com.platon.rosettaflow.common.constants.SysConfig;
import com.platon.rosettaflow.common.constants.SysConstant;
import com.platon.rosettaflow.common.enums.MetaDataExpireStatusEnum;
import com.platon.rosettaflow.common.utils.AddressChangeUtils;
import com.platon.rosettaflow.common.utils.RedisUtil;
import com.platon.rosettaflow.grpc.metadata.resp.dto.GetMetaDataAuthorityDto;
import com.platon.rosettaflow.grpc.service.GrpcAuthService;
import com.platon.rosettaflow.mapper.domain.UserMetaData;
import com.platon.rosettaflow.service.IUserMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hudenian
 * @date 2021/8/25
 * @description 同步用户元数据授权列列表（当用户对元数据做授权申请后，redis记录设置已申请，此定时任务判断有待申请记录就启动同步，否则不同步）
 */
@Slf4j
@Component
@Profile({"prod", "test", "local"})
public class SyncUserDataAuthTask {

    @Resource
    private SysConfig sysConfig;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private GrpcAuthService grpcAuthService;

    @Resource
    private IUserMetaDataService userMetaDataService;

    @Scheduled(fixedDelay = 3 * 1000, initialDelay = 2 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        if (!sysConfig.isMasterNode()) {
            return;
        }
        //test begin 模拟用户申请数据授权
        redisUtil.listLeftPush(SysConstant.REDIS_SYNC_USER_METADATA_PREFIX_KEY,"0x990a168ecee09b8b1abeff3e2b713924e7151f9bmetadata:0x6ada026b4219b186ea7c7c60bb350615419e87c7ff7104ec4f95cd81c96793d7",null);
        //test end

        //如果用户没有申请过元数据，则跳过
        long applyNum = redisUtil.listSize(SysConstant.REDIS_SYNC_USER_METADATA_PREFIX_KEY);
        if (applyNum < 1) {
            return;
        }
        //所有需要同步的元数据
        Set<String> userMetaDataIdSet = new HashSet<>();
        //net中还未查询到的元数据
        Set<String> userMetaDataIdNetNoExistSet = new HashSet<>();
        try {
            log.info("用户申请授权元数据信息同步开始>>>>");
            long begin;
            List<GetMetaDataAuthorityDto> metaDataAuthorityDtoList;
            try {
                metaDataAuthorityDtoList = grpcAuthService.getGlobalMetadataAuthorityList();
            } catch (Exception e) {
                log.error("从net同步用户元数据授权列表失败,失败原因：{}", e.getMessage());
                return;
            }
            //查看需要同步数据是否已存在
            for (int i = 0; i < applyNum; i++) {
                String userMetaDataId = redisUtil.listRightPop(SysConstant.REDIS_SYNC_USER_METADATA_PREFIX_KEY);
                userMetaDataIdSet.add(userMetaDataId);
                userMetaDataIdNetNoExistSet.add(userMetaDataId);
            }
            //判断是否有需要同步数据
            boolean syncFlg = false;
            for (GetMetaDataAuthorityDto authorityDto : metaDataAuthorityDtoList) {
                String resultUserMetaDataId = AddressChangeUtils.convert0xAddress(authorityDto.getUser()) + authorityDto.getMetaDataAuthorityDto().getMetaDataId();
                if (userMetaDataIdSet.contains(resultUserMetaDataId)) {
                    syncFlg = true;
                    userMetaDataIdNetNoExistSet.remove(resultUserMetaDataId);
                }
            }
            if (!syncFlg) {
                return;
            }
            List<UserMetaData> userMetaDataList = new ArrayList<>();
            UserMetaData userMetaData;
            int userMetaDataSize = 0;

            for (GetMetaDataAuthorityDto authorityDto : metaDataAuthorityDtoList) {
                userMetaData = getUserMetaData(authorityDto);

                userMetaDataList.add(userMetaData);
                ++userMetaDataSize;
                if (userMetaDataSize % sysConfig.getBatchSize() == 0) {
                    begin = DateUtil.currentSeconds();
                    log.info("用户申请授权元数据据更新{}条数据开始", userMetaDataSize);
                    userMetaDataService.batchInsert(userMetaDataList);
                    log.info("用户申请授权元数据据更新{}条数据结束一共用时{}秒", userMetaDataSize, DateUtil.currentSeconds() - begin);
                    userMetaDataList.clear();
                }
            }
            if (userMetaDataList.size() > 0) {
                begin = DateUtil.currentSeconds();
                log.info("用户申请授权元数据据更新{}条数据开始", userMetaDataSize);
                userMetaDataService.batchInsert(userMetaDataList);
                log.info("用户申请授权元数据据更新{}条数据结束一共用时{}秒", userMetaDataSize, DateUtil.currentSeconds() - begin);
            }
            log.info("用户申请授权元数据信息同步结束>>>>");

            //未同步数据放回redis
            for (String value : userMetaDataIdNetNoExistSet) {
                redisUtil.listLeftPush(SysConstant.REDIS_SYNC_USER_METADATA_PREFIX_KEY, value, null);
            }
        } catch (Exception e) {
            log.error("用户申请授权元数据信息同步失败，失败原因>>>>{}", e.getMessage(), e);
            //未同步数据放回redis
            for (String value : userMetaDataIdSet) {
                redisUtil.listLeftPush(SysConstant.REDIS_SYNC_USER_METADATA_PREFIX_KEY, value, null);
            }
        }
    }

    private UserMetaData getUserMetaData(GetMetaDataAuthorityDto authorityDto) {
        UserMetaData userMetaData;
        userMetaData = new UserMetaData();
        userMetaData.setMetaDataId(authorityDto.getMetaDataAuthorityDto().getMetaDataId());
        userMetaData.setIdentityId(authorityDto.getMetaDataAuthorityDto().getOwner().getIdentityId());
        userMetaData.setIdentityName(authorityDto.getMetaDataAuthorityDto().getOwner().getNodeName());
        userMetaData.setNodeId(authorityDto.getMetaDataAuthorityDto().getOwner().getNodeId());

        String address;
        try {
            address = AddressChangeUtils.convert0xAddress(authorityDto.getUser());
            userMetaData.setAddress(address);
        } catch (Exception e) {
            log.error("钱包地址{}非法", authorityDto.getUser(), e);
            userMetaData.setAddress(authorityDto.getUser());
        }

        userMetaData.setAuthType(authorityDto.getMetaDataAuthorityDto().getMetaDataUsageDto().getUseType().byteValue());
        //授权次数
        Integer times = authorityDto.getMetaDataAuthorityDto().getMetaDataUsageDto().getTimes();
        if (null != times && times > 0) {
            userMetaData.setAuthValue(times);
        }
        //授权开始时间
        Long authBeginTime = authorityDto.getMetaDataAuthorityDto().getMetaDataUsageDto().getStartAt();
        if (null != authBeginTime && authBeginTime > 0) {
            userMetaData.setAuthBeginTime(DateUtil.date(authBeginTime));
        }

        //授权结束时间
        Long authEndTime = authorityDto.getMetaDataAuthorityDto().getMetaDataUsageDto().getEndAt();
        if (null != authEndTime && authEndTime > 0) {
            userMetaData.setAuthEndTime(DateUtil.date(authEndTime));
        }

        int auditStatus = authorityDto.getAuditMetaDataOption();
        userMetaData.setAuthStatus((byte) auditStatus);
        userMetaData.setApplyTime(DateUtil.date(authorityDto.getApplyAt()));
        userMetaData.setAuditTime(DateUtil.date(authorityDto.getAuditAt()));
        userMetaData.setExpire(authorityDto.getMetadataUsedQuoDto().isExpire() ? MetaDataExpireStatusEnum.expire.getValue() : MetaDataExpireStatusEnum.un_expire.getValue());
        userMetaData.setUsedTimes((long) authorityDto.getMetadataUsedQuoDto().getUsedTimes());
        userMetaData.setAuthMetadataState(authorityDto.getMetadataAuthorityState().byteValue());
        userMetaData.setAuditSuggestion(authorityDto.getAuditSuggestion());
        return userMetaData;
    }
}
