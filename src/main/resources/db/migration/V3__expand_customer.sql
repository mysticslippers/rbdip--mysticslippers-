ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255);

UPDATE customers SET first_name = split_part(btrim(full_name), ' ', 1);

UPDATE customers SET last_name = btrim(substr(btrim(full_name), strpos(btrim(full_name), ' ') + 1))
                 WHERE strpos(btrim(full_name), ' ') > 0;

CREATE FUNCTION synchronize_customer_name()
RETURNS trigger AS $$
    DECLARE write_parts BOOLEAN;
    BEGIN
        IF TG_OP = 'INSERT' THEN
            write_parts := NEW.full_name IS NULL;
        ELSE
            write_parts := NEW.full_name IS NOT DISTINCT FROM OLD.full_name;
        END IF;

        IF write_parts THEN
            NEW.full_name := concat_ws(' ', NEW.first_name, NEW.last_name);
        ELSE
            NEW.first_name := split_part(btrim(NEW.full_name), ' ', 1);
            IF strpos(btrim(NEW.full_name), ' ') > 0 THEN
                NEW.last_name := btrim(substr(btrim(NEW.full_name), strpos(btrim(NEW.full_name), ' ') + 1));
            ELSE
                NEW.last_name := NULL;
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER customers_name_synchronization
BEFORE INSERT OR UPDATE OF full_name, first_name, last_name ON customers
FOR EACH ROW
EXECUTE FUNCTION synchronize_customer_name();

CREATE FUNCTION fill_legacy_order_customer()
RETURNS trigger AS $$
    BEGIN
        IF NEW.customer_id IS NOT NULL THEN
            SELECT customer.full_name, customer.address, customer.phone
                INTO NEW.customer_full_name, NEW.customer_address, NEW.customer_phone
                    FROM customers customer
                        WHERE customer.id = NEW.customer_id;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER orders_legacy_customer
BEFORE INSERT OR UPDATE OF customer_id ON orders
FOR EACH ROW
EXECUTE FUNCTION fill_legacy_order_customer();

CREATE FUNCTION fill_legacy_item_product()
RETURNS trigger AS $$
    BEGIN
        IF NEW.product_id IS NOT NULL THEN
            SELECT product.name, product.price
                INTO NEW.product_name, NEW.product_price
                    FROM products product
                        WHERE product.id = NEW.product_id;
        END IF;
        RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_items_legacy_product
BEFORE INSERT OR UPDATE OF product_id ON order_items
FOR EACH ROW
EXECUTE FUNCTION fill_legacy_item_product();
