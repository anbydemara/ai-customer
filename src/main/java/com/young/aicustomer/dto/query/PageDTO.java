package com.young.aicustomer.dto.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageDTO<T> {
    private Long total;
    private Long pages;
    private List<T> list;

    public static <PO, VO> PageDTO<VO> of(Page<PO> page, Class<VO> clazz) {
        PageDTO<VO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(page.getTotal());
        pageDTO.setPages(page.getPages());
        List<PO> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            pageDTO.setList(Collections.emptyList());
            return pageDTO;
        }
        // 泛型无法获取字节码，所以需要参数传递
        pageDTO.setList(BeanUtil.copyToList(records, clazz));
        return pageDTO;
    }

    // 但上述方法转化过程只是简单的属性同名copy，没有其他特殊处理，可以自定义特殊处理
    // 要自定义处理则需要传递行为，使用Function作为参数
    public static <PO, VO> PageDTO<VO> of(Page<PO> page, Function<PO, VO> convertor) {
        PageDTO<VO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(page.getTotal());
        pageDTO.setPages(page.getPages());
        List<PO> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            pageDTO.setList(Collections.emptyList());
            return pageDTO;
        }
        // 使用自定义处理行为
        pageDTO.setList(records.stream().map(convertor).collect(Collectors.toList()));
        return pageDTO;
    }
}
