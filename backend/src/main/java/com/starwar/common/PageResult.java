package com.starwar.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Integer total;

    public PageResult(List<T> list, Integer total) {
        this.list = list;
        this.total = total;
    }
}


