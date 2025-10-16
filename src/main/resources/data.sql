-- Let Hibernate create the schema; only insert seed data matching JPA naming
INSERT INTO invoices (id, invoice_number, customer_name, issue_date, due_date, status, amount_paid) VALUES
  (1, 'INV-1001', 'Acme Corp', DATE '2025-09-15', DATE '2025-09-30', 'UNPAID', 0),
  (2, 'INV-1002', 'Globex Inc', DATE '2025-09-20', DATE '2025-10-05', 'PARTIALLY_PAID', 150.00),
  (3, 'INV-1003', 'Soylent Co', DATE '2025-08-01', DATE '2025-08-15', 'OVERDUE', 0);

INSERT INTO invoice_items (description, quantity, unit_price, invoice_id) VALUES
  ('Consulting Services', 10, 100.00, 1),
  ('Maintenance', 5, 50.00, 1),
  ('Software License', 1, 500.00, 2),
  ('Support Hours', 5, 50.00, 2),
  ('Subscription', 12, 25.00, 3);

-- Ensure auto-increment continues after explicit IDs
ALTER TABLE invoices ALTER COLUMN id RESTART WITH 4;
