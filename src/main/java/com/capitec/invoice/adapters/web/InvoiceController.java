package com.capitec.invoice.adapters.web;

import com.capitec.invoice.adapters.web.dto.*;
import com.capitec.invoice.domain.model.Invoice;
import com.capitec.invoice.domain.model.InvoiceItem;
import com.capitec.invoice.domain.ports.InvoiceServicePort;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceServicePort service;
    private final TemplateEngine templateEngine;

    public InvoiceController(InvoiceServicePort service, TemplateEngine templateEngine) {
        this.service = service;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/invoices")
    public List<InvoiceDto> list() {
        return service.list().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDto> get(@PathVariable Long id) {
        return service.get(id).map(inv -> ResponseEntity.ok(toDto(inv))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceDto> create(@Validated @RequestBody InvoiceDto dto) {
        Invoice saved = service.create(fromDto(dto));
        return ResponseEntity.created(URI.create("/api/invoices/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDto> update(@PathVariable Long id, @Validated @RequestBody InvoiceDto dto) {
        Invoice saved = service.update(id, fromDto(dto));
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<InvoiceDto> pay(@PathVariable Long id, @Validated @RequestBody PaymentRequest request) {
        Invoice saved = service.recordPayment(id, request.amount);
        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/invoices/overdue")
    public List<InvoiceDto> overdue() {
        return service.listOverdue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/summary")
    public SummaryDto summary() {
        InvoiceServicePort.Summary s = service.summary();
        SummaryDto dto = new SummaryDto();
        dto.totalInvoices = s.totalInvoices;
        dto.paidCount = s.paidCount;
        dto.unpaidCount = s.unpaidCount;
        dto.overdueCount = s.overdueCount;
        dto.totalOutstanding = s.totalOutstanding;
        dto.totalPaid = s.totalPaid;
        return dto;
    }

    @GetMapping(value = "/invoices/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) throws Exception {
        Invoice inv = service.get(id).orElse(null);
        if (inv == null) return ResponseEntity.notFound().build();
        Context ctx = new Context();
        ctx.setVariable("invoice", inv);
        String html = templateEngine.process("invoice-pdf", ctx);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();
        byte[] pdfBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + inv.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private InvoiceDto toDto(Invoice inv) {
        InvoiceDto dto = new InvoiceDto();
        dto.id = inv.getId();
        dto.invoiceNumber = inv.getInvoiceNumber();
        dto.customerName = inv.getCustomerName();
        dto.issueDate = inv.getIssueDate();
        dto.dueDate = inv.getDueDate();
        dto.status = inv.getStatus();
        dto.amountPaid = inv.getAmountPaid();
        dto.items = inv.getItems().stream().map(it -> {
            InvoiceItemDto i = new InvoiceItemDto();
            i.id = it.getId();
            i.description = it.getDescription();
            i.quantity = it.getQuantity();
            i.unitPrice = it.getUnitPrice();
            return i;
        }).collect(Collectors.toList());
        return dto;
    }

    private Invoice fromDto(InvoiceDto dto) {
        Invoice inv = new Invoice();
        inv.setId(dto.id);
        inv.setInvoiceNumber(dto.invoiceNumber);
        inv.setCustomerName(dto.customerName);
        inv.setIssueDate(dto.issueDate);
        inv.setDueDate(dto.dueDate);
        inv.setAmountPaid(dto.amountPaid == null ? BigDecimal.ZERO : dto.amountPaid);
        inv.setItems(dto.items == null ? List.of() : dto.items.stream().map(d -> {
            InvoiceItem it = new InvoiceItem();
            it.setId(d.id);
            it.setDescription(d.description);
            it.setQuantity(d.quantity);
            it.setUnitPrice(d.unitPrice);
            return it;
        }).collect(Collectors.toList()));
        inv.setStatus(dto.status);
        return inv;
    }
}
