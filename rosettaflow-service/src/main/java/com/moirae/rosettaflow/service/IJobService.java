package com.moirae.rosettaflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moirae.rosettaflow.dto.JobDto;
import com.moirae.rosettaflow.mapper.domain.Job;
import com.moirae.rosettaflow.mapper.domain.Workflow;

import java.util.List;

/**
 * @author hudenian
 * @date 2021/8/13
 * @description 作业管理服务
 */
public interface IJobService extends IService<Job> {
    /**
     * 获取所有待处理的作业
     *
     * @return 待处理作业列表
     */
    List<Job> getAllUnfinishedJob();

    /**
     * 获取作业分页列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param jobName 作业名称
     * @param projectId 项目id
     * @return 分页数据
     */
    IPage<JobDto> list(Long current, Long size, String jobName, Long projectId);

    /**
     * 获取有效状态的job
     * @param id 作业id
     * @return job 作业对象
     */
    Job getValidJobById(Long id);

    /**
     * 批量修改作业有效状态
     *
     * @param ids 作业ids
     * @param status  有效状态
     */
    void updateBatchStatus(Object[] ids, Byte status);

    /**
     * 添加作业
     *
     * @param jobDto 添加作业请求对象
     */
    void add(JobDto jobDto);

    /**
     * 编辑作业
     *
     * @param jobDto 编辑作业请求对象
     */
    void edit(JobDto jobDto);

    /**
     * 编辑作业基本信息
     *
     * @param jobDto 编辑作业请求对象
     */
    void editBasicInfo(JobDto jobDto);

    /**
     * 查询关联工作流
     *
     * @param projectId 项目id
     * @return  List<Workflow> 工作流集合
     */
    List<Workflow> queryRelatedWorkflowName(Long projectId);

    /**
     * 暂停作业
     *
     * @param id 作业Id
     */
    void pause(Long id);

    /**
     * 重启作业
     *
     * @param id 作业Id
     */
    void reStart(Long id);

    /**
     * 根据工作流id获取所有正在运行作业列表
     *
     * @param workflowId 工作流id
     * @return 作业列表
     */
    List<Job> listRunJobByWorkflowId(Long workflowId);

    /**
     * 获取所有正在运行作业列表
     *
     * @return 作业列表
     */
    List<Job> listRunAllJob();

    /**
     *  批量删除作业
     * @param ids 作业ids集合
     */
    void deleteBatchJob(List<Long> ids);

    /**
     * 是否存在指定的作业名称
     * @param jobName
     * @return 是否存在作业
     */
    boolean isExistJobName(String jobName);


}
