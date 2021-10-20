package com.platon.rosettaflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platon.rosettaflow.common.enums.StatusEnum;
import com.platon.rosettaflow.mapper.TaskResultMapper;
import com.platon.rosettaflow.mapper.domain.TaskResult;
import com.platon.rosettaflow.service.ITaskResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/10/14
 */
@Slf4j
@Service
public class TaskResultServiceImpl extends ServiceImpl<TaskResultMapper, TaskResult> implements ITaskResultService {


    @Override
    public TaskResult queryTaskResultByTaskId(String taskId) {
        LambdaQueryWrapper<TaskResult> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TaskResult::getTaskId, taskId);
        queryWrapper.eq(TaskResult::getStatus, StatusEnum.VALID.getValue());
        return this.getOne(queryWrapper);
    }

    @Override
    public void batchInsert(List<TaskResult> taskResultList) {
        this.baseMapper.batchInsert(taskResultList);
    }


}