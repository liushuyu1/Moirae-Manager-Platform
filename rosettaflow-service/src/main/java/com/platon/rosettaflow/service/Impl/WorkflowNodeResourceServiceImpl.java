package com.platon.rosettaflow.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platon.rosettaflow.mapper.WorkflowNodeResourceMapper;
import com.platon.rosettaflow.mapper.domain.WorkflowNodeResource;
import com.platon.rosettaflow.service.IWorkflowNodeResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author hudenian
 * @date 2021/8/18
 * @description 工作流节点资源服务实现类
 */
@Slf4j
@Service
public class WorkflowNodeResourceServiceImpl extends ServiceImpl<WorkflowNodeResourceMapper, WorkflowNodeResource> implements IWorkflowNodeResourceService {
    @Override
    public WorkflowNodeResource getByWorkflowNodeId(Long workflowNodeId) {
        LambdaQueryWrapper<WorkflowNodeResource> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNodeResource::getWorkflowNodeId, workflowNodeId);
        return this.getOne(wrapper);
    }
}