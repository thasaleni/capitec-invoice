package com.capitec.invoice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private PaymentStatus status;
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public Invoice(Long id) { this.id = id; }

    public BigDecimal getSubtotal() {
        return items.stream().map(InvoiceItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() { return getSubtotal(); }

    public BigDecimal getBalanceDue() { return getTotal().subtract(amountPaid == null ? BigDecimal.ZERO : amountPaid); }

    public boolean isOverdue() {
        return status != PaymentStatus.PAID && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
