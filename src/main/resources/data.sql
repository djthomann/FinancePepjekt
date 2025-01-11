INSERT INTO stock (symbol, description, figi, currency)
VALUES ('AAPL', 'Apple Inc.', 'BBG000B9XRY4', 'USD'),
       ('TSLA', 'Tesla Inc.', 'BBG000N9MNX3', 'USD'),
       ('GOOGL', 'Alphabet Inc.', 'BBG001S5N8V8', 'USD'),
       ('SAP', 'SAP SE', 'BBG000BB1CX2', 'EUR');

INSERT INTO finance_owner (id, name, mail)
VALUES (1, 'Alice Smith', 'alice.smith@example.com'),
       (2, 'Bob Johnson', 'bob.johnson@example.com'),
       (3, 'Charlie Brown', 'charlie.brown@example.com');

INSERT INTO quote (current_price,
                   change,
                   percent_change,
                   high_price_of_the_day,
                   low_price_of_the_day,
                   open_price_of_the_day,
                   previous_close_price,
                   time_stamp,
                   stock_symbol)
VALUES (182.50, 2.5, 1.39, 185.00, 180.00, 181.00, 180.00, '2024-12-17 10:30:00', 'AAPL'),
       (250.75, -1.25, -0.50, 255.00, 249.00, 252.00, 252.00, '2024-12-17 11:00:00', 'TSLA'),
       (132.20, 0.75, 0.57, 133.50, 131.00, 131.50, 131.00, '2024-12-17 12:15:00', 'GOOGL'),
       (123.45, -0.55, -0.44, 124.00, 123.00, 123.80, 124.00, '2024-12-17 13:00:00', 'SAP');


INSERT INTO bankaccount (currency, balance)
VALUES ('USD', 1000.50),
       ('EUR', 250.00),
       ('JPY', 500000.00),
       ('GBP', 123.45);


-- Investment Accounts
INSERT INTO investmentaccount (bank_account_id, owner_id)
VALUES (1, 1), -- Verknüpft mit Bankkonto ID 1 und Benutzer ID 1
       (2, 2), -- Verknüpft mit Bankkonto ID 2 und Benutzer ID 2
       (3, 3);
-- Verknüpft mit Bankkonto ID 3 und Benutzer ID 3

-- Portfolio
INSERT INTO portfolio_entry (investment_account_id, stock_symbol, quantity)
VALUES (1, 'AAPL', 50.5),
       (1, 'TSLA', 10.0),
       (1, 'GOOGL', 5.0),
       (2, 'SAP', 15.0),
       (3, 'AAPL', 25.0);

-- 
INSERT INTO stock_order (volume, type, portfolio_entry_id, stock_symbol)
VALUES (100.0, 'BUY', 1, 'AAPL'), -- Order for 100 AAPL from Investmentkonto 1
       (50.0, 'SELL', 1, 'TSLA'), -- Order for 50 TSLA from Investmentkonto 1
       (25.5, 'BUY', 2, 'GOOGL'), -- Order for 25.5 GOOGL from Investmentkonto 2
       (10.0, 'SELL', 3, 'SAP');
-- Order for 10 SAP from Investmentkonto 3

INSERT INTO exchange (symbol, description)
VALUES ('NYSE', 'New York Stock Exchange'),
       ('NASDAQ', 'NASDAQ Stock Market'),
       ('LSE', 'London Stock Exchange');