-- Beispiel-Datensätze
INSERT INTO stock (symbol, description, figi, currency)
VALUES ('AAPL', 'Apple Inc.', 'BBG000B9XRY4', 'USD'),
       ('TSLA', 'Tesla Inc.', 'BBG000N9MNX3', 'USD'),
       ('GOOGL', 'Alphabet Inc.', 'BBG001S5N8V8', 'USD'),
       ('SAP', 'SAP SE', 'BBG000BB1CX2', 'EUR');

-- Beispiel-Datensätze
INSERT INTO finance_user (id, name, mail)
VALUES (1, 'Alice Smith', 'alice.smith@example.com'),
       (2, 'Bob Johnson', 'bob.johnson@example.com'),
       (3, 'Charlie Brown', 'charlie.brown@example.com');

-- Beispiel-Datensätze
INSERT INTO quote (value, timeStamp, stock_symbol)
VALUES (182.50, '2024-12-17 10:30:00', 'AAPL'),
       (250.75, '2024-12-17 11:00:00', 'TSLA'),
       (132.20, '2024-12-17 12:15:00', 'GOOGL'),
       (123.45, '2024-12-17 13:00:00', 'SAP');

-- Beispiel-Datensätze
INSERT INTO bankaccount (currency, balance)
VALUES ('USD', 1000.50),
       ('EUR', 250.00),
       ('JPY', 500000.00),
       ('GBP', 123.45);

-- Beispiel-Datensätze
-- Investment Accounts
INSERT INTO investmentaccount (bank_account_id, owner_id)
VALUES (1, 1), -- Verknüpft mit Bankkonto ID 1 und Benutzer ID 1
       (2, 2), -- Verknüpft mit Bankkonto ID 2 und Benutzer ID 2
       (3, 3);
-- Verknüpft mit Bankkonto ID 3 und Benutzer ID 3

-- Portfolio
INSERT INTO investment_portfolio (investment_account_id, stock_symbol, quantity)
VALUES (1, 'AAPL', 50.5),
       (1, 'TSLA', 10.0),
       (2, 'GOOGL', 5.0),
       (2, 'SAP', 15.0),
       (3, 'AAPL', 25.0);

-- Beispiel-Datensätze
INSERT INTO stock_order (volume, type, investment_account_id, stock_symbol)
VALUES (100.0, 'BUY', 1, 'AAPL'), -- Order für 100 AAPL von Investmentkonto 1
       (50.0, 'SELL', 1, 'TSLA'), -- Order für 50 TSLA von Investmentkonto 1
       (25.5, 'BUY', 2, 'GOOGL'), -- Order für 25.5 GOOGL von Investmentkonto 2
       (10.0, 'SELL', 3, 'SAP');
-- Order für 10 SAP von Investmentkonto 3

-- Beispiel-Datensätze
-- Börsen
INSERT INTO exchange (symbol, description)
VALUES ('NYSE', 'New York Stock Exchange'),
       ('NASDAQ', 'NASDAQ Stock Market'),
       ('LSE', 'London Stock Exchange');