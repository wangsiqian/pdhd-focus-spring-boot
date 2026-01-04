package com.pdhd.server.service;

import com.pdhd.server.pojo.req.ListPLanReq;
import com.pdhd.server.pojo.req.ListScheduleReq;
import com.pdhd.server.pojo.req.ScheduleReq;
import com.pdhd.server.pojo.resp.PlanDTO;
import com.pdhd.server.pojo.resp.ScheduleDTO;

import java.util.List;

/**
 * @author pdhd
 */
public interface ScheduleService {
    /**
     * 通过ID获取计划信息，仅允许获取当前用户计划
     *
     * @param id 计划ID
     * @return 计划信息
     * @author pdhd
     */
    ScheduleDTO getById(Long id);

    /**
     * 获取当前登录用户的计划列表
     *
     * @param req 查询参数
     * @return 计划列表
     * @author pdhd
     */
    List<ScheduleDTO> list(ListScheduleReq req);

    /**
     * 新增或更新计划
     *
     * @param scheduleReq 计划信息
     * @return 创建或更新后的计划信息
     * @author pdhd
     */
    ScheduleDTO upsert(ScheduleReq scheduleReq);

    /**
     * 删除计划，仅允许删除当前用户的计划
     *
     * @param id 计划ID
     * @author pdhd
     */
    void delete(Long id);

    /**
     * 获取时间返回的计划
     */
    List<PlanDTO> plan(ListPLanReq req);
}