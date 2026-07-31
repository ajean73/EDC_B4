package com.shopwise.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shopwise.app.entity.Sale;

public interface ISalesRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = { "lines", "lines.product" })
    List<Sale> findAllByOrderBySoldAtDesc();

    @EntityGraph(attributePaths = { "lines", "lines.product" })
    Optional<Sale> findWithLinesById(Long id);
}