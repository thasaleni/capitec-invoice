package com.capitec.invoice.adapters.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotNull
    public BigDecimal amount;
}
