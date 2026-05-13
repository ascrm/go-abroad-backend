package com.goAbroad.auth.dto;

import lombok.Data;

@Data
public class SwitchAccountRequest {
    private Integer accountType;
    private String accountValue;
}