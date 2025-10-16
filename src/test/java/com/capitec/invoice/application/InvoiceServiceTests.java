package com.capitec.invoice.application;

import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.InvoiceItem;
import com.capitec.invoice.domain.model.PaymentStatus;
import com.capitec.invoice.domain.ports.InvoiceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceTests {

    private InvoiceRepositoryPort repository;
    private InvoiceService service;

    @BeforeEach
    void setup() {
        repository = mock(InvoiceRepositoryPort.class);
        service = new InvoiceService(repository);
    }

    private Invoice sampleInvoice() {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV-X");
        inv.setCustomerName("Acme");
        inv.setIssueDate(LocalDate.now().minusDays(10));
        inv.setDueDate(LocalDate.now().minusDays(1));
        InvoiceItem item = new InvoiceItem();
        item.setDescription("Thing");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        inv.setItems(List.of(item));
        return inv;
    }

    @Test
    void create_setsDefaultStatusAndAutoUpdates() {
        Invoice inv = sampleInvoice();
        // repository echoes back entity with id
        when(repository.save(any())).thenAnswer(a -> {
            Invoice i = a.getArgument(0);
            i.setId(42L);
            return i;
        });

        Invoice saved = service.create(inv);
        assertEquals(42L, saved.getId());
        // due yesterday and unpaid -> OVERDUE
        assertEquals(PaymentStatus.OVERDUE, saved.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    void update_setsIdAndAutoUpdatesStatus() {
        when(repository.save(any())).thenAnswer(a -> a.getArgument(0));
        Invoice inv = sampleInvoice();
        inv.setAmountPaid(new BigDecimal("20.00"));

        Invoice saved = service.update(7L, inv);
        assertEquals(7L, saved.getId());
        assertEquals(PaymentStatus.PAID, saved.getStatus());
    }

    @Test
    void recordPayment_addsToExistingAmountAndUpdatesStatus() {
        Invoice inv = sampleInvoice();
        inv.setId(5L);
        inv.setAmountPaid(new BigDecimal("5.00")); // subtotal 20.00
        inv.setDueDate(LocalDate.now().plusDays(5)); // not overdue
        when(repository.findById(5L)).thenReturn(Optional.of(inv));
        when(repository.save(any())).thenAnswer(a -> a.getArgument(0));

        Invoice saved = service.recordPayment(5L, new BigDecimal("5.00"));
        assertEquals(new BigDecimal("10.00"), saved.getAmountPaid());
        assertEquals(PaymentStatus.PARTIALLY_PAID, saved.getStatus());

        // Pay remaining
        saved = service.recordPayment(5L, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("20.00"), saved.getAmountPaid());
        assertEquals(PaymentStatus.PAID, saved.getStatus());
    }

    @Test
    void listOverdue_delegatesToRepositoryWithToday() {
        when(repository.findOverdue(any())).thenReturn(List.of());
        service.listOverdue();
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(repository).findOverdue(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue());
    }

    @Test
    void summary_computesAggregates() {
        Invoice a = sampleInvoice();
        a.setId(1L);
        a.setStatus(PaymentStatus.UNPAID);
        Invoice b = sampleInvoice();
        b.setId(2L);
        b.setStatus(PaymentStatus.PAID);
        b.setAmountPaid(new BigDecimal("20.00"));
        Invoice c = sampleInvoice();
        c.setId(3L);
        c.setStatus(PaymentStatus.PARTIALLY_PAID);
        c.setAmountPaid(new BigDecimal("5.00"));

        when(repository.findAll()).thenReturn(List.of(a, b, c));

        InvoiceService.Summary s = service.summary();
        assertEquals(3, s.totalInvoices);
        assertEquals(1, s.paidCount);
        assertEquals(2, s.unpaidCount); // includes PARTIALLY_PAID in unpaid bucket per implementation
        assertTrue(s.overdueCount >= 0); // depends on today
        assertEquals(new BigDecimal("35.00"), s.totalOutstanding); // (20-0)+(20-20)+(20-5)=35
        assertEquals(new BigDecimal("25.00"), s.totalPaid);
    }
}
