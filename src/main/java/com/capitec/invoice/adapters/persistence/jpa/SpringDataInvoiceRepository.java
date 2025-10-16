package com.capitec.invoice.adapters.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    @Query("select i from InvoiceEntity i where i.status <> 'PAID' and i.dueDate < :today")
    List<InvoiceEntity> findOverdue(LocalDate today);
}
