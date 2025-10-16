package com.capitec.invoice.adapters.persistence;

import com.capitec.invoice.adapters.persistence.jpa.InvoiceEntity;
import com.capitec.invoice.adapters.persistence.jpa.InvoiceItemEntity;
import com.capitec.invoice.adapters.persistence.jpa.SpringDataInvoiceRepository;
import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.InvoiceItem;
import com.capitec.invoice.domain.model.PaymentStatus;
import com.capitec.invoice.domain.ports.InvoiceRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final SpringDataInvoiceRepository jpaRepo;

    public InvoiceRepositoryAdapter(SpringDataInvoiceRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = toEntity(invoice);
        // handle bidirectional
        entity.getItems().forEach(it -> it.setInvoice(entity));
        InvoiceEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public List<Invoice> findOverdue(LocalDate today) {
        return jpaRepo.findOverdue(today).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private InvoiceEntity toEntity(Invoice inv) {
        InvoiceEntity e = new InvoiceEntity();
        e.setId(inv.getId());
        e.setInvoiceNumber(inv.getInvoiceNumber());
        e.setCustomerName(inv.getCustomerName());
        e.setIssueDate(inv.getIssueDate());
        e.setDueDate(inv.getDueDate());
        e.setStatus(inv.getStatus() == null ? null : inv.getStatus().name());
        e.setAmountPaid(inv.getAmountPaid());
        List<InvoiceItemEntity> items = inv.getItems() == null ? List.of() : inv.getItems().stream().map(it -> {
            InvoiceItemEntity ie = new InvoiceItemEntity();
            ie.setId(it.getId());
            ie.setDescription(it.getDescription());
            ie.setQuantity(it.getQuantity());
            ie.setUnitPrice(it.getUnitPrice());
            return ie;
        }).collect(Collectors.toList());
        e.setItems(items);
        return e;
    }

    private Invoice toDomain(InvoiceEntity e) {
        Invoice inv = new Invoice();
        inv.setId(e.getId());
        inv.setInvoiceNumber(e.getInvoiceNumber());
        inv.setCustomerName(e.getCustomerName());
        inv.setIssueDate(e.getIssueDate());
        inv.setDueDate(e.getDueDate());
        inv.setStatus(e.getStatus() == null ? null : PaymentStatus.valueOf(e.getStatus()));
        inv.setAmountPaid(e.getAmountPaid());
        List<InvoiceItem> items = e.getItems() == null ? List.of() : e.getItems().stream().map(ie -> {
            InvoiceItem it = new InvoiceItem();
            it.setId(ie.getId());
            it.setDescription(ie.getDescription());
            it.setQuantity(ie.getQuantity());
            it.setUnitPrice(ie.getUnitPrice());
            return it;
        }).collect(Collectors.toList());
        inv.setItems(items);
        return inv;
    }
}
