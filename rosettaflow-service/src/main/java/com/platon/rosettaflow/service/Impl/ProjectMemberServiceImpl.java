package com.platon.rosettaflow.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platon.rosettaflow.mapper.ProjectMemberMapper;
import com.platon.rosettaflow.mapper.domain.ProjectMember;
import com.platon.rosettaflow.service.IProjectMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @date 2021/8/16
 */
@Slf4j
@Service
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements IProjectMemberService {


    @Override
    public List<ProjectMember> queryByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ProjectMember::getProjectId, projectId);
        return this.list(queryWrapper);
    }

    @Override
    public void updateBatchById(List<Long> idList) {
        this.updateBatchById(updateDelVersionById(idList));
    }

    /** 修改版本标识，解决逻辑删除唯一校验问题 */
    private List<ProjectMember> updateDelVersionById(List<Long> idList){
        List<ProjectMember> projectMemberList = new ArrayList<>();
        if (idList != null && idList.size() > 0) {
            idList.forEach(id -> {
                ProjectMember projectMember = new ProjectMember();
                projectMember.setId(id);
                projectMember.setDelVersion(id);
                projectMemberList.add(projectMember);
            });
        }
        return projectMemberList;
    }
}
