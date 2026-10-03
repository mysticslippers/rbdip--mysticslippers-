package com.rbdip.bookstore.order.persistence;

import com.rbdip.bookstore.order.domain.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findFirstByFullNameAndAddressAndPhone(String fullName, String address, String phone);
}
