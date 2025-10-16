package com.capitec.invoice.application;

import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.PaymentStatus;
import com.capitec.invoice.domain.ports.InvoiceRepositoryPort;
import com.capitec.invoice.domain.ports.InvoiceServicePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class InvoiceService implements InvoiceServicePort {

    private final InvoiceRepositoryPort repository;

    public InvoiceService(InvoiceRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Invoice create(Invoice invoice) {
        if (invoice.getStatus() == null) {
            invoice.setStatus(PaymentStatus.UNPAID);
        }
        autoUpdateStatus(invoice);
        return repository.save(invoice);
    }

    @Override
    public Optional<Invoice> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Invoice> list() {
        return repository.findAll();
    }

    @Override
    public Invoice update(Long id, Invoice invoice) {
        invoice.setId(id);
        autoUpdateStatus(invoice);
        return repository.save(invoice);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Invoice recordPayment(Long id, BigDecimal amount) {
        Invoice inv = repository.findById(id).orElseThrow();
        BigDecimal paid = inv.getAmountPaid() == null ? BigDecimal.ZERO : inv.getAmountPaid();
        inv.setAmountPaid(paid.add(amount == null ? BigDecimal.ZERO : amount));
        autoUpdateStatus(inv);
        return repository.save(inv);
    }

    @Override
    public List<Invoice> listOverdue() {
        return repository.findOverdue(LocalDate.now());
    }

    @Override
    public Summary summary() {
        List<Invoice> all = repository.findAll();
        Summary s = new Summary();
        s.totalInvoices = all.size();
        s.paidCount = all.stream().filter(i -> i.getStatus() == PaymentStatus.PAID).count();
        s.overdueCount = all.stream().filter(Invoice::isOverdue).count();
        s.unpaidCount = s.totalInvoices - s.paidCount;
        s.totalOutstanding = all.stream().map(Invoice::getBalanceDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        s.totalPaid = all.stream().map(i -> i.getAmountPaid() == null ? BigDecimal.ZERO : i.getAmountPaid()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return s;
    }

    private void autoUpdateStatus(Invoice inv) {
        if (inv.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            inv.setStatus(PaymentStatus.PAID);
        } else if (inv.isOverdue()) {
            inv.setStatus(PaymentStatus.OVERDUE);
        } else if (inv.getAmountPaid() != null && inv.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            inv.setStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            inv.setStatus(PaymentStatus.UNPAID);
        }
    }
}
