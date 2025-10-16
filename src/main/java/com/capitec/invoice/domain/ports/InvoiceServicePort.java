package com.capitec.invoice.domain.ports;

import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceServicePort {
    Invoice create(Invoice invoice);
    Optional<Invoice> get(Long id);
    List<Invoice> list();
    Invoice update(Long id, Invoice invoice);
    void delete(Long id);
    Invoice recordPayment(Long id, BigDecimal amount);
    List<Invoice> listOverdue();
    Summary summary();

    class Summary {
        public long totalInvoices;
        public long paidCount;
        public long unpaidCount;
        public long overdueCount;
        public BigDecimal totalOutstanding;
        public BigDecimal totalPaid;
    }
}
