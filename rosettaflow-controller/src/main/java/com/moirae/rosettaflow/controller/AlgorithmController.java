package com.moirae.rosettaflow.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moirae.rosettaflow.common.enums.AlgorithmTypeEnum;
import com.moirae.rosettaflow.common.utils.BeanCopierUtils;
import com.moirae.rosettaflow.dto.AlgorithmDto;
import com.moirae.rosettaflow.req.algorithm.AlgListReq;
import com.moirae.rosettaflow.req.algorithm.AlgorithmReq;
import com.moirae.rosettaflow.service.IAlgorithmService;
import com.moirae.rosettaflow.vo.PageVo;
import com.moirae.rosettaflow.vo.ResponseVo;
import com.moirae.rosettaflow.vo.algorithm.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 算法相关接口
 *
 * @author admin
 * @date 2021/8/17
 */
@Slf4j
@RestController
@Api(tags = "算法相关接口")
@RequestMapping(value = "algorithm", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlgorithmController {

    @Resource
    private IAlgorithmService algorithmService;

    @PostMapping("addAlgorithm")
    @ApiOperation(value = "新增算法", notes = "新增算法")
    public ResponseVo<?> addAlgorithm(@RequestBody @Valid AlgorithmReq algorithmReq) {
        algorithmService.addAlgorithm(BeanUtil.copyProperties(algorithmReq, AlgorithmDto.class));
        return ResponseVo.createSuccess();

    }

    @PostMapping("updateAlgorithm")
    @ApiOperation(value = "修改算法", notes = "修改算法")
    public ResponseVo<?> updateAlgorithm(@RequestBody @Valid AlgorithmReq algorithmReq) {
        algorithmService.updateAlgorithm(BeanUtil.copyProperties(algorithmReq, AlgorithmDto.class));
        return ResponseVo.createSuccess();
    }

    @GetMapping("list")
    @ApiOperation(value = "查询算法列表", notes = "查询算法列表")
    public ResponseVo<PageVo<AlgorithmListVo>> list(@Valid AlgListReq algListReq) {
        IPage<AlgorithmDto> algorithmDtoIpage = algorithmService.queryAlgorithmList(algListReq.getCurrent(), algListReq.getSize(), algListReq.getAlgorithmName());
        return convertAlgorithmDtoToResponseVo(algorithmDtoIpage);
    }

    @GetMapping("details/{id}")
    @ApiOperation(value = "查询算法详情", notes = "查询算法详情")
    public ResponseVo<AlgDetailsVo> detail(@ApiParam(value = "算法表ID", required = true) @PathVariable Long id) {
        AlgorithmDto algorithmDto = algorithmService.queryAlgorithmDetails(id);
        AlgDetailsVo algDetailsVo = BeanUtil.copyProperties(algorithmDto, AlgDetailsVo.class);
        algDetailsVo.setAlgorithmTypeDesc(AlgorithmTypeEnum.getName(algorithmDto.getAlgorithmType()));
        return ResponseVo.createSuccess(algDetailsVo);
    }

    @GetMapping("queryAlgorithmTreeList")
    @ApiOperation(value = "查询算法树列表", notes = "查询算法树列表")
    public ResponseVo<AlgTreeListVo> queryAlgorithmTreeList(HttpServletRequest request) {
        String language = request.getHeader("Accept-Language");
        List<Map<String, Object>> listVo = algorithmService.queryAlgorithmTreeList(language);
        AlgTreeListVo algTreeListVo = this.convertAlgTreeList(listVo);
        return ResponseVo.createSuccess(algTreeListVo);
    }

    /**
     * 查询算法树响应参数转换
     */
    @SuppressWarnings("unchecked")
    private AlgTreeListVo convertAlgTreeList(List<Map<String, Object>> listVo) {
        AlgTreeListVo algTreeListVo = new AlgTreeListVo();
        List<AlgTreeVo> algTreeVoList = new ArrayList<>();
        for (Map<String, Object> map : listVo) {
            AlgTreeVo algTreeVo = BeanUtil.toBean(map, AlgTreeVo.class);
            List<Map<String, Object>> childList = (List<Map<String, Object>>) map.get("child");
            if (null == childList || childList.size() == 0) {
                algTreeVoList.add(algTreeVo);
                continue;
            }
            List<AlgChildTreeVo> algDetailsVoList = new ArrayList<>();
            childList.forEach(param -> {
                AlgChildTreeVo algChildTreeVo = BeanUtil.toBean(param, AlgChildTreeVo.class);
                AlgDetailsVo algDetailsVo = BeanUtil.toBean(param.get("algorithmDto"), AlgDetailsVo.class);
                algChildTreeVo.setAlgDetailsVo(algDetailsVo);
                algDetailsVoList.add(algChildTreeVo);
            });
            algTreeVo.setChild(algDetailsVoList);
            algTreeVoList.add(algTreeVo);
        }
        algTreeListVo.setAlgTreeVoList(algTreeVoList);
        return algTreeListVo;
    }

    /**
     * 转换算法分页列表
     * @param algorithmDtoIpage 算法分页page
     * @return 算法列表
     */
    private ResponseVo<PageVo<AlgorithmListVo>> convertAlgorithmDtoToResponseVo(IPage<AlgorithmDto> algorithmDtoIpage) {
        List<AlgorithmListVo> items = new ArrayList<>();
        algorithmDtoIpage.getRecords().forEach(algorithmDto -> {
            AlgorithmListVo algorithmListVo = new AlgorithmListVo();
            BeanCopierUtils.copy(algorithmDto, algorithmListVo);
            items.add(algorithmListVo);
        });

        PageVo<AlgorithmListVo> pageVo = new PageVo<>();
        BeanUtil.copyProperties(algorithmDtoIpage, pageVo);
        pageVo.setItems(items);

        return ResponseVo.createSuccess(pageVo);
    }

}
