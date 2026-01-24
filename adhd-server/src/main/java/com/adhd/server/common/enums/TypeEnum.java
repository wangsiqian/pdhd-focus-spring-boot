package com.adhd.server.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TypeEnum {
    WORK("工作"),
    INVEST("投资"),
    STUDY("学习"),
    LIFE("生活"),
    ENTERTAINMENT("娱乐"),
    OTHER("其它");

    private final String desc;
}
