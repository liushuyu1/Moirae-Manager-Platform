package com.moirae.rosettaflow.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moirae.rosettaflow.common.constants.SysConstant;
import com.moirae.rosettaflow.dto.WorkflowDto;
import com.moirae.rosettaflow.dto.WorkflowNodeDto;
import com.moirae.rosettaflow.grpc.task.req.dto.TaskEventDto;
import com.moirae.rosettaflow.mapper.domain.Workflow;
import com.moirae.rosettaflow.req.workflow.*;
import com.moirae.rosettaflow.service.IWorkflowNodeService;
import com.moirae.rosettaflow.service.IWorkflowService;
import com.moirae.rosettaflow.utils.ConvertUtils;
import com.moirae.rosettaflow.vo.PageVo;
import com.moirae.rosettaflow.vo.ResponseVo;
import com.moirae.rosettaflow.vo.workflow.GetStatusVo;
import com.moirae.rosettaflow.vo.workflow.TaskEventVo;
import com.moirae.rosettaflow.vo.workflow.WorkflowDetailsVo;
import com.moirae.rosettaflow.vo.workflow.WorkflowVo;
import com.moirae.rosettaflow.vo.workflow.node.GetNodeStatusVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 工作流管理
 *
 * @author admin
 * @date 2021/8/17
 */
@Slf4j
@RestController
@Api(tags = "工作流管理关接口")
@RequestMapping(value = "workflow", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowController {

    @Resource
    private IWorkflowService workflowService;

    @Resource
    private IWorkflowNodeService workflowNodeService;

    @GetMapping("list")
    @ApiOperation(value = "查询工作流列表", notes = "查询工作流列表")
    public ResponseVo<PageVo<WorkflowVo>> list(@Validated ListWorkflowReq listReq) {
        IPage<WorkflowDto> page = workflowService.queryWorkFlowPageList(listReq.getProjectId(),
                listReq.getWorkflowName(), listReq.getCurrent(), listReq.getSize());
        List<WorkflowVo> items = BeanUtil.copyToList(page.getRecords(), WorkflowVo.class);
        return ResponseVo.createSuccess(ConvertUtils.convertPageVo(page, items));
    }

    @GetMapping(value = "detail/{id}")
    @ApiOperation(value = "获取工作流详情", notes = "获取工作流详情")
    public ResponseVo<WorkflowDetailsVo> detail(@ApiParam(value = "工作流表主键ID", required = true) @PathVariable Long id) {
        Workflow workflow = workflowService.queryWorkflowDetail(id);
        return ResponseVo.createSuccess(BeanUtil.toBean(workflow, WorkflowDetailsVo.class));
    }

    @PostMapping("add")
    @ApiOperation(value = "添加工作流", notes = "添加工作流")
    public ResponseVo<?> add(@RequestBody @Validated AddWorkflowReq addReq) {
        Workflow workflow = BeanUtil.toBean(addReq, Workflow.class);
        workflowService.addWorkflow(workflow);
        return ResponseVo.createSuccess();
    }

    @PostMapping("edit")
    @ApiOperation(value = "编辑工作流", notes = "编辑工作流")
    public ResponseVo<?> edit(@RequestBody @Validated EditWorkflowReq editReq) {
        workflowService.editWorkflow(editReq.getId(), editReq.getWorkflowName(), editReq.getWorkflowDesc());
        return ResponseVo.createSuccess();
    }

    @PostMapping("delete/{id}")
    @ApiOperation(value = "删除工作流", notes = "删除工作流")
    public ResponseVo<?> delete(@ApiParam(value = "工作流表ID", required = true) @PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseVo.createSuccess();
    }

    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除工作流", notes = "批量删除工作流")
    public ResponseVo<?> deleteBatch(@RequestBody @Validated DeleteBatchReq deleteBatchReq) {
        workflowService.deleteWorkflowBatch(deleteBatchReq.getIds());
        return ResponseVo.createSuccess();
    }

    @PostMapping("copy")
    @ApiOperation(value = "复制工作流", notes = "复制工作流")
    public ResponseVo<?> copy(@RequestBody @Validated CopyWorkflowReq copyReq) {
        workflowService.copyWorkflow(copyReq.getOriginId(), copyReq.getWorkflowName(), copyReq.getWorkflowDesc());
        return ResponseVo.createSuccess();
    }

    @PostMapping("start")
    @ApiOperation(value = "启动工作流", notes = "启动工作流")
    public ResponseVo<?> start(@RequestBody @Validated StartWorkflowReq startWorkflowReq) {
        //先保存工作流
        if (SysConstant.STR_1.equals(startWorkflowReq.getSaveFlag())) {
            List<WorkflowNodeDto> workflowNodeDtoList = ConvertUtils.convertSaveReq(
                    startWorkflowReq.getWorkflowNodeReqList(), Boolean.TRUE);
            workflowNodeService.saveWorkflowAllNodeData(startWorkflowReq.getWorkflowId(), workflowNodeDtoList);
        }

        //启动工作流
        WorkflowDto workflowDto = BeanUtil.toBean(startWorkflowReq, WorkflowDto.class);
        workflowDto.setId(startWorkflowReq.getWorkflowId());
        workflowService.start(workflowDto);
        return ResponseVo.createSuccess();
    }

    @PostMapping("getLog/{workflowId}")
    @ApiOperation(value = "获取运行日志", notes = "获取运行日志")
    public ResponseVo<List<TaskEventVo>> getLog(@ApiParam(value = "workflowId", required = true) @PathVariable Long workflowId) {
        List<TaskEventDto> taskEventShowDtoList = workflowService.getTaskEventList(workflowId);
        return ResponseVo.createSuccess(getLogConvertVo(taskEventShowDtoList));
    }

    /** 获取日志接口返回参数转换 */
    private  List<TaskEventVo> getLogConvertVo(List<TaskEventDto> taskEventShowDtoList){
        List<TaskEventVo> taskEventVoList = new ArrayList<>();
        TaskEventVo vo;
        if (taskEventShowDtoList.size() > 0) {
            for (TaskEventDto taskEventDto : taskEventShowDtoList) {
                vo = new TaskEventVo();
                vo.setType(taskEventDto.getType());
                vo.setTaskId(taskEventDto.getTaskId());
                vo.setName(taskEventDto.getOwner().getNodeName());
                vo.setNodeId(taskEventDto.getOwner().getNodeId());
                vo.setIdentityId(taskEventDto.getOwner().getIdentityId());
                vo.setPartyId(taskEventDto.getPartyId());
                vo.setContent(taskEventDto.getContent());
                vo.setCreateAt(DateUtil.date(taskEventDto.getCreateAt()));
                taskEventVoList.add(vo);
            }
        }
        return taskEventVoList;
    }

    @PostMapping("terminate")
    @ApiOperation(value = "终止工作流", notes = "终止工作流")
    public ResponseVo<?> terminate(@RequestBody @Validated TerminateWorkflowReq terminateWorkflowReq) {
        workflowService.terminate(terminateWorkflowReq.getWorkflowId());
        return ResponseVo.createSuccess();
    }

    @GetMapping("getWorkflowStatus")
    @ApiOperation(value = "获取工作流状态", notes = "获取工作流状态")
    public ResponseVo<GetStatusVo> getWorkflowStatus(@Validated GetStatusReq getStatusReq) {
        Map<String, Object> map = workflowService.getWorkflowStatusById(getStatusReq.getId());
        return ResponseVo.createSuccess(convertGetStatusVo(map));
    }

    /**
     * 获取工作流运行状态返回参数转换
     */
    private GetStatusVo convertGetStatusVo(Map<String, Object> map) {
        GetStatusVo getStatusVo = BeanUtil.toBean(map, GetStatusVo.class);
        List<GetNodeStatusVo> getNodeStatusVoList = BeanUtil.copyToList((Collection<?>) map.get("nodeList"), GetNodeStatusVo.class);
        getStatusVo.setGetNodeStatusVoList(getNodeStatusVoList);
        return getStatusVo;
    }

}
