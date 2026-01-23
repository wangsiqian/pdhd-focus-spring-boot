package com.adhd.server.service;

import com.adhd.server.pojo.resp.ActivityDTO;
import com.adhd.server.pojo.req.ActivityReq;
import com.adhd.server.pojo.req.ListActivityReq;

import java.util.List;

/**
 * @author adhd
 */
public interface ActivityService {
    /**
     * 通过ID获取实际事项信息，仅允许获取当前用户实际事项
     *
     * @param id 实际事项ID
     * @return 实际事项信息
     * @author adhd
     */
    ActivityDTO getById(Long id);

    /**
     * 获取当前登录用户的实际事项列表
     *
     * @param req 查询参数
     * @return 实际事项列表
     * @author adhd
     */
    List<ActivityDTO> list(ListActivityReq req);

    /**
     * 新增或更新实际事项
     *
     * @param activityReq 实际事项信息
     * @return 创建或更新后的实际事项信息
     * @author adhd
     */
    ActivityDTO upsert(ActivityReq activityReq);

    /**
     * 删除实际事项，仅允许删除当前用户的实际事项
     *
     * @param id 实际事项ID
     * @author adhd
     */
    void delete(Long id);
}