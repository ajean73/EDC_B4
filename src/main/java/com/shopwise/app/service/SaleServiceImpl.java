package com.shopwise.app.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.dto.response.SaleSummaryResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.SaleMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    public SaleServiceImpl(SaleRepository saleRepository, ProductRepository productRepository, SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.saleMapper = saleMapper;
    }

    @Override
    public SaleResponse create(CreateSaleRequest request) {
        Map<Long, Product> productsById = loadProductsById(request.getLines());

        Sale sale = new Sale();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateSaleItemRequest lineRequest : request.getLines()) {
            Product product = productsById.get(lineRequest.getProductId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(lineRequest.getQuantity()));

            SaleItem line = new SaleItem();
            line.setProduct(product);
            line.setQuantity(lineRequest.getQuantity());
            line.setUnitPrice(unitPrice);
            line.setItemTotal(itemTotal);

            sale.addLine(line);
            total = total.add(itemTotal);
        }

        sale.setTotalAmount(total);

        Sale saved = saleRepository.save(sale);
        return saleMapper.toResponse(saved);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SaleSummaryResponse> getAll() {
        List<Sale> sales = saleRepository.findAllByOrderBySoldAtDesc();
        return saleMapper.toSummaryList(sales);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findWithLinesById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found"));
        return saleMapper.toResponse(sale);
    }

    private Map<Long, Product> loadProductsById(List<CreateSaleItemRequest> lines) {
        Set<Long> productIds = new HashSet<>();
        for (CreateSaleItemRequest line : lines) {
            productIds.add(line.getProductId());
        }

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productsById = new HashMap<>();
        for (Product product : products) {
            productsById.put(product.getId(), product);
        }

        for (Long productId : productIds) {
            if (!productsById.containsKey(productId)) {
                throw new NotFoundException("Product not found with id: " + productId);
            }
        }

        return productsById;
    }
}
