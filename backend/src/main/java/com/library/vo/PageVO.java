package com.library.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页结果")
public class PageVO<T> {

    @Schema(description = "当前页数据")
    private List<T> list;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码，从1开始")
    private int page;

    @Schema(description = "每页条数")
    private int size;
}
