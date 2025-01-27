package com.moirae.rosettaflow.grpc.identity.dto;

import lombok.Data;

/**
 * @author hudenian
 * @date 2021/8/4
 * @description 组织信息
 */
@Data
public class OrganizationIdentityInfoDto {
    /**
     * 任务里面定义的 (p0 -> pN 方 ...)
     */
    private String partyId;

    /**
     * 组织名称
     */
    private String nodeName;

    /**
     * 组织中调度服务的 nodeId
     */
    private String nodeId;

    /**
     * 机构身份标识ID
     */
    private String identityId;
}
