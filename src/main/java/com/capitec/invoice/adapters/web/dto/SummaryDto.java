package com.capitec.invoice.adapters.web.dto;

import java.math.BigDecimal;

public class SummaryDto {
    public long totalInvoices;
    public long paidCount;
    public long unpaidCount;
    public long overdueCount;
    public BigDecimal totalOutstanding;
    public BigDecimal totalPaid;
}
