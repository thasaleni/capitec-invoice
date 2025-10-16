package com.capitec.invoice.adapters.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InvoiceControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void list_returnsSeededInvoices() throws Exception {
        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].invoiceNumber", not(emptyString())));
    }

    @Test
    void get_returnsInvoiceOrNotFound() throws Exception {
        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Acme Corp")))
                .andExpect(jsonPath("$.items", hasSize(greaterThan(0))));

        mockMvc.perform(get("/api/invoices/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_update_pay_and_delete_flow() throws Exception {
        // create
        String newInvoiceJson = "{\n" +
                "  \"invoiceNumber\": \"INV-NEW\",\n" +
                "  \"customerName\": \"New Co\",\n" +
                "  \"issueDate\": \"2025-10-01\",\n" +
                "  \"dueDate\": \"2025-10-31\",\n" +
                "  \"items\": [ { \"description\": \"Service\", \"quantity\": 2, \"unitPrice\": 50.00 } ]\n" +
                "}";

        String location = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newInvoiceJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/invoices/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getHeader("Location");

        // update
        String updatedJson = "{\n" +
                "  \"invoiceNumber\": \"INV-NEW\",\n" +
                "  \"customerName\": \"New Co Updated\",\n" +
                "  \"issueDate\": \"2025-10-01\",\n" +
                "  \"dueDate\": \"2025-10-31\",\n" +
                "  \"items\": [ { \"description\": \"Service\", \"quantity\": 2, \"unitPrice\": 50.00 } ]\n" +
                "}";

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("New Co Updated")));

        // pay
        mockMvc.perform(post(location + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountPaid", is(50.0)));

        // delete
        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isNotFound());
    }

    @Test
    void overdue_and_summary_endpoints() throws Exception {
        mockMvc.perform(get("/api/invoices/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", anyOf(is("OVERDUE"), is("UNPAID"), is("PARTIALLY_PAID"))));

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvoices", is(3)))
                .andExpect(jsonPath("$.totalOutstanding", greaterThanOrEqualTo(0.0)));
    }

}
