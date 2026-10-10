package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "为已有书目批量新增副本")
public class CopyBatchAddDTO {

    @NotEmpty
    @Valid
    private List<BookAcquireDTO.CopyItem> copies;
}
