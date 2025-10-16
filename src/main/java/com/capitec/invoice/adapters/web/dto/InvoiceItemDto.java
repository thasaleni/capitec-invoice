package com.capitec.invoice.adapters.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InvoiceItemDto {
    public Long id;
    @NotBlank
    public String description;
    @Min(1)
    public int quantity;
    @NotNull
    public BigDecimal unitPrice;
}
