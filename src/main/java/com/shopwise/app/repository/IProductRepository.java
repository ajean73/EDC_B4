package com.shopwise.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopwise.app.entity.Product;

public interface IProductRepository extends JpaRepository<Product, Long> {

}