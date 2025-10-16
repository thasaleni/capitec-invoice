package com.capitec.invoice.domain.ports;

import com.capitec.invoice.domain.model.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    List<Invoice> findAll();
    void deleteById(Long id);
    List<Invoice> findOverdue(LocalDate today);
}
