package com.shopwise.app.service;

import java.util.List;

import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.dto.response.SaleSummaryResponse;

public interface ISalesService {

    SaleResponse create(CreateSaleRequest request);

    List<SaleSummaryResponse> getAll();

    SaleResponse getById(Long id);
}