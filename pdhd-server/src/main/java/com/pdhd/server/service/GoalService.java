package com.pdhd.server.service;

import com.pdhd.server.pojo.resp.GoalDTO;
import com.pdhd.server.req.GoalReq;
import com.pdhd.server.req.ListGoalReq;

import java.util.List;

/**
 * @author pdhd
 */
public interface GoalService {
    /**
     * 通过ID获取目标信息，仅允许获取当前用户的目标
     *
     * @param id 目标ID
     * @return 目标信息
     * @author pdhd
     */
    GoalDTO getById(Long id);

    /**
     * 获取当前登录用户的目标列表
     *
     * @param req 查询参数
     * @return 目标列表
     * @author pdhd
     */
    List<GoalDTO> list(ListGoalReq req);

    /**
     * 新增或更新目标
     *
     * @param goalReq 目标信息
     * @return 创建或更新后的目标信息
     * @author pdhd
     */
    GoalDTO upsert(GoalReq goalReq);

    /**
     * 删除目标，仅允许删除当前用户的目标
     *
     * @param id 目标ID
     * @author pdhd
     */
    void delete(Long id);
}