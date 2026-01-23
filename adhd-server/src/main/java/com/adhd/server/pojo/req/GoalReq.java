package com.adhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author adhd
 */
@Data
public class GoalReq {
    private Long id;
    
    @NotBlank(message = "目标标题不能为空")
    private String title;
    
    private String color;
    private Integer status;
    private Integer progress;
}