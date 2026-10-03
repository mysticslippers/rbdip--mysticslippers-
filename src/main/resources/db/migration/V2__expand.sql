CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

ALTER TABLE orders ADD COLUMN customer_id BIGINT REFERENCES customers(id);

INSERT INTO customers (full_name, address, phone)
    SELECT DISTINCT customer_full_name, customer_address, customer_phone FROM orders;


UPDATE orders ord SET customer_id = customer.id
    FROM customers customer WHERE ord.customer_full_name = customer.full_name
        AND ((ord.customer_address = customer.address) OR (ord.customer_address IS NULL AND customer.address IS NULL))
            AND ((ord.customer_phone = customer.phone) OR (ord.customer_phone IS NULL AND customer.phone IS NULL));

ALTER TABLE order_items ADD COLUMN product_id BIGINT REFERENCES products(id);

INSERT INTO products (name, price)
    SELECT DISTINCT order_item.product_name, order_item.product_price FROM order_items order_item
        WHERE NOT EXISTS (
            SELECT 1 FROM products product
                     WHERE product.name = order_item.product_name AND product.price = order_item.product_price
        );

UPDATE order_items order_item SET product_id = (
    SELECT MIN (product.id) FROM products product
        WHERE product.name = order_item.product_name AND product.price = order_item.product_price
);