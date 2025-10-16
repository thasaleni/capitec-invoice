package com.capitec.invoice.domain;

import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.InvoiceItem;
import com.capitec.invoice.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceDomainTests {

    private Invoice sampleInvoice() {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV-1");
        inv.setCustomerName("Acme");
        inv.setIssueDate(LocalDate.now().minusDays(10));
        inv.setDueDate(LocalDate.now().plusDays(5));
        inv.setStatus(PaymentStatus.UNPAID);
        InvoiceItem i1 = new InvoiceItem();
        i1.setDescription("A");
        i1.setQuantity(2);
        i1.setUnitPrice(new BigDecimal("10.50"));
        InvoiceItem i2 = new InvoiceItem();
        i2.setDescription("B");
        i2.setQuantity(1);
        i2.setUnitPrice(new BigDecimal("5.00"));
        inv.getItems().add(i1);
        inv.getItems().add(i2);
        return inv;
    }

    @Test
    void subtotalAndTotalAreSumOfLineItems() {
        Invoice inv = sampleInvoice();
        assertEquals(new BigDecimal("26.00"), inv.getSubtotal());
        assertEquals(new BigDecimal("26.00"), inv.getTotal());
    }

    @Test
    void balanceDueSubtractsAmountPaid() {
        Invoice inv = sampleInvoice();
        inv.setAmountPaid(new BigDecimal("6.00"));
        assertEquals(new BigDecimal("20.00"), inv.getBalanceDue());
    }

    @Test
    void overdueOnlyWhenNotPaidAndPastDueDate() {
        Invoice inv = sampleInvoice();
        inv.setDueDate(LocalDate.now().minusDays(1));
        assertTrue(inv.isOverdue());
        inv.setStatus(PaymentStatus.PAID);
        assertFalse(inv.isOverdue());
    }
}
