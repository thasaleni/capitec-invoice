package com.capitec.invoice.adapters.web.dto;

import com.capitec.invoice.domain.model.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class InvoiceDto {
    public Long id;
    public String invoiceNumber;
    @NotBlank
    public String customerName;
    @NotNull
    public LocalDate issueDate;
    @NotNull
    public LocalDate dueDate;
    public PaymentStatus status;
    public java.math.BigDecimal amountPaid;
    @Valid
    public List<InvoiceItemDto> items;
}
