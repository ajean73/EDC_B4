CREATE TABLE SALES (
    id BIGSERIAL PRIMARY KEY,
    sold_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL
);

CREATE TABLE SALE_ITEMS (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    item_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES SALES(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(id)
);

CREATE INDEX idx_sale_items_sale_id ON SALE_ITEMS(sale_id);
CREATE INDEX idx_sale_items_product_id ON SALE_ITEMS(product_id);
CREATE INDEX idx_sales_sold_at ON SALES(sold_at);
