package com.moirae.rosettaflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moirae.rosettaflow.mapper.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
public interface UserMapper extends BaseMapper<User> {

    List<User> queryUserByProjectId(@Param("projectId") Long projectId);
}