package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * 分页查询通用参数
 */
@Data//自动装配get,set
@ToString//自动专配toString()
public class PageParams {

    //当前页码
    private Long pageNo = 1L;

    //每页记录数默认值
    private Long pageSize =10L;

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public PageParams() {
    }
}
