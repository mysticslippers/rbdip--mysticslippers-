package com.rbdip.bookstore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    private static final int ADDRESS_LENGTH = 500;
    private static final int PHONE_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address", length = ADDRESS_LENGTH)
    private String address;

    @Column(name = "phone", length = PHONE_LENGTH)
    private String phone;

    protected Customer() {}

    public Customer(String fullName, String address, String phone) {
        String[] nameParts = fullName.trim().split(" +", 2);
        this.firstName = nameParts[0];
        this.lastName = nameParts.length == 2 ? nameParts[1] : null;
        this.address = address;
        this.phone = phone;
    }

    public Long getId() {
        return this.id;
    }

    public String getFullName() {
        if (this.lastName == null || this.lastName.isBlank())
            return this.firstName;

        return this.firstName + " " + this.lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPhone() {
        return this.phone;
    }
}
