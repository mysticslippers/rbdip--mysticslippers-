INSERT INTO customers (full_name, address, phone)
    SELECT DISTINCT ord.customer_full_name, ord.customer_address, ord.customer_phone FROM orders ord
        WHERE ord.customer_id IS NULL AND NOT EXISTS (
        SELECT 1 FROM customers customer
                 WHERE customer.full_name = ord.customer_full_name
                   AND ((ord.customer_address = customer.address) OR (ord.customer_address IS NULL AND customer.address IS NULL))
                        AND ((ord.customer_phone = customer.phone) OR (ord.customer_phone IS NULL AND customer.phone IS NULL)));

UPDATE orders ord SET customer_id = (
    SELECT MIN(customer.id) FROM customers customer
        WHERE customer.full_name = ord.customer_full_name
          AND ((ord.customer_address = customer.address) OR (ord.customer_address IS NULL AND customer.address IS NULL))
            AND ((ord.customer_phone = customer.phone) OR (ord.customer_phone IS NULL AND customer.phone IS NULL))
) WHERE ord.customer_id IS NULL;

UPDATE customers SET first_name = split_part(btrim(full_name), ' ', 1)
                 WHERE first_name IS NULL;

UPDATE customers SET last_name = btrim(substr(btrim(full_name), strpos(btrim(full_name), ' ') + 1))
                 WHERE last_name IS NULL AND strpos(btrim(full_name), ' ') > 0;

INSERT INTO products (name, price)
    SELECT DISTINCT item.product_name, item.product_price FROM order_items item
        WHERE item.product_id IS NULL AND NOT EXISTS (
            SELECT 1 FROM products product WHERE product.name = item.product_name
                AND product.price = item.product_price
        );

UPDATE order_items item SET product_id = (
    SELECT MIN(product.id) FROM products product
        WHERE product.name = item.product_name
            AND product.price = item.product_price
) WHERE item.product_id IS NULL;

DROP TRIGGER order_items_legacy_product ON order_items;
DROP FUNCTION fill_legacy_item_product();

DROP TRIGGER orders_legacy_customer ON orders;
DROP FUNCTION fill_legacy_order_customer();

DROP TRIGGER customers_name_synchronization ON customers;
DROP FUNCTION synchronize_customer_name();

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;
ALTER TABLE customers ALTER COLUMN first_name SET NOT NULL;

ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;

ALTER TABLE customers DROP COLUMN full_name;
