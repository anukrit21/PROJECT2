package com.demoApp.common_kafka.event.user;

import lombok.Data;

@Data
public class UserAddress {
    private String street;
    private String city;
    private String state;
    private String zipCode;
}
