package com.platon.rosettaflow.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platon.rosettaflow.mapper.WorkflowNodeVariableMapper;
import com.platon.rosettaflow.mapper.domain.WorkflowNodeVariable;
import com.platon.rosettaflow.service.IWorkflowNodeVariableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/18
 * @description 工作流节点变量服务实现类
 */
@Slf4j
@Service
public class WorkflowNodeVariableServiceImpl extends ServiceImpl<WorkflowNodeVariableMapper, WorkflowNodeVariable> implements IWorkflowNodeVariableService {
    @Override
    public List<WorkflowNodeVariable> getByWorkflowNodeId(Long workflowNodeId) {
        LambdaQueryWrapper<WorkflowNodeVariable> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkflowNodeVariable::getWorkflowNodeId, workflowNodeId);
        return this.list(wrapper);
    }
}