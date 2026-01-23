package com.adhd.server.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ZoneTypeEnum {
    COMFORT("舒适区"),
    STRETCH("拉伸区"),
    DIFFICULTY("困难区");

    private final String desc;
}