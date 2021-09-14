package com.platon.rosettaflow.mapper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * t_workflow_node_input
 *
 * @author admin
 */
@Data
@TableName(value = "t_workflow_node_input")
public class WorkflowNodeInput implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 工作流节点ID(自增长)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 工作流节点id
     */
    private Long workflowNodeId;
    /**
     * 数据类型：1:结构化数据，2:非结构化数据
     */
    private Byte dataType;
    /**
     * 组织的身份标识Id
     */
    private String identityId;
    /**
     * 资源所属组织中调度服务的 nodeId
     */
    private String nodeId;
    /**
     * 数据表ID
     */
    private String dataTableId;
    /**
     * 数据字段ID
     */
    private String dataColumnIds;
    /**
     * 数据文件id
     */
    private String dataFileId;
    /**
     * 任务里面定义的 (p0 -> pN 方 ...)
     */
    private String partyId;
    /**
     * 状态: 0-无效，1- 有效
     */
    @TableField(value = "`status`")
    private Byte status;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}