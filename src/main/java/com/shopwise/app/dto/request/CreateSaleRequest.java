package com.shopwise.app.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class CreateSaleRequest {

    @NotEmpty
    @Valid
    private List<CreateSaleItemRequest> lines;

    public List<CreateSaleItemRequest> getLines() {
        return lines;
    }

    public void setLines(List<CreateSaleItemRequest> lines) {
        this.lines = lines;
    }
}
