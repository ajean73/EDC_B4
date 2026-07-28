package com.shopwise.app.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.shopwise.app.dto.response.SaleItemResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.dto.response.SaleSummaryResponse;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale sale) {
        SaleResponse response = new SaleResponse();
        response.setId(sale.getId());
        response.setSoldAt(sale.getSoldAt());
        response.setTotalAmount(sale.getTotalAmount());
        response.setLines(sale.getLines().stream().map(this::toItemResponse).toList());
        return response;
    }

    public SaleSummaryResponse toSummary(Sale sale) {
        SaleSummaryResponse summary = new SaleSummaryResponse();
        summary.setId(sale.getId());
        summary.setSoldAt(sale.getSoldAt());
        summary.setTotalAmount(sale.getTotalAmount());
        summary.setLineCount(sale.getLines().size());
        return summary;
    }

    public List<SaleSummaryResponse> toSummaryList(List<Sale> sales) {
        return sales.stream().map(this::toSummary).toList();
    }

    private SaleItemResponse toItemResponse(SaleItem line) {
        SaleItemResponse response = new SaleItemResponse();
        response.setProductId(line.getProduct().getId());
        response.setProductName(line.getProduct().getName());
        response.setQuantity(line.getQuantity());
        response.setUnitPrice(line.getUnitPrice());
        response.setItemTotal(line.getItemTotal());
        return response;
    }
}
