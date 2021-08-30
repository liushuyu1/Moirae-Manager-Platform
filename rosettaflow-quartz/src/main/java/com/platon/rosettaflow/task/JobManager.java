package com.platon.rosettaflow.task;

import com.platon.rosettaflow.common.constants.SysConfig;
import com.platon.rosettaflow.common.enums.JobRepeatEnum;
import com.platon.rosettaflow.common.enums.RespCodeEnum;
import com.platon.rosettaflow.common.exception.BusinessException;
import com.platon.rosettaflow.mapper.domain.Job;
import com.platon.rosettaflow.quartz.job.PublishTaskJob;
import com.platon.rosettaflow.service.IJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/13
 * @description 作业管理服务
 */
@Slf4j
@Component
public class JobManager {

    @Resource
    private Scheduler scheduler;

    @Resource
    private SysConfig sysConfig;

    @Resource
    private IJobService jobService;

    /**
     * 服务启动加载所有的job
     */
    @PostConstruct
    public void init() {
        if (sysConfig.isMasterNode()) {
            List<Job> jobList = jobService.getAllUnfinishedJob();
            for (Job job : jobList) {
                startJob(job, false);
            }
        }
    }

    /**
     * 创建一个job信息
     *
     * @param job       job信息
     * @param modifyFlg 是否修改已有的job
     */
    public void startJob(Job job, boolean modifyFlg) {
        if (modifyFlg) {
            try {
                //暂停触发器
                scheduler.pauseTrigger(TriggerKey.triggerKey(job.getId().toString()));
                //移除触发器
                scheduler.unscheduleJob(TriggerKey.triggerKey(job.getId().toString()));
                //删除Job
                scheduler.deleteJob(JobKey.jobKey(job.getId().toString()));
            } catch (SchedulerException e) {
                log.error("stop old job error,error msg is:{}", e.getMessage(), e);
            }
        }
        Long workflowId = job.getWorkflowId();

        JobDetail jobDetail = JobBuilder.newJob(PublishTaskJob.class)
                .usingJobData("workflowId", workflowId)
                .usingJobData("jobId", job.getId())
                .withIdentity(job.getId().toString())
                .build();

        SimpleScheduleBuilder simpleScheduleBuilder;
        if (JobRepeatEnum.NOREPEAT.getValue() == job.getRepeatFlag()) {
            simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(job.getRepeatInterval())
                    .withRepeatCount(1);
        } else {
            simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(job.getRepeatInterval())
                    .repeatForever();
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString())
                .startAt(job.getBeginTime())
                .endAt(job.getEndTime())
                .withSchedule(simpleScheduleBuilder)
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            log.info("作业id:{}启动成功>>>>>>", job.getId());
        } catch (SchedulerException e) {
            log.error("作业id:{}启动失败>>>>>>", job.getId(), e);
            throw new BusinessException(RespCodeEnum.EXCEPTION);
        }
    }
}