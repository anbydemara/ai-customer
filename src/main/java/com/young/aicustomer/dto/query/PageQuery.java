package com.young.aicustomer.dto.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageQuery {
    private Long pageNo = 1L;
    private Long pageSize = 5L;
    private String sortBy = "create_time";
    private Boolean isAsc = false;

    public <T> Page<T> toMpPageDefault() {
        // 1. 分页条件
        Page<T> page = Page.of(pageNo, pageSize);
        // 2. 排序条件（默认）
        if (StrUtil.isNotBlank(sortBy)) {    // 不为空，按要求排序
            page.addOrder(new OrderItem(sortBy, isAsc));
        }
        return page;
    }
}
