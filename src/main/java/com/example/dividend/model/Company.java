package com.example.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // getter, setter, toString, equalAndHashcode, requiredArgumentConstructor, values annotation 포함.
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private String ticker;
    private String name;
}
