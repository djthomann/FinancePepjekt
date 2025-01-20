INSERT INTO stock (symbol, name, description, figi, currency)
VALUES ('AAPL', 'Apple', 'Apple Inc.', 'BBG000B9XRY4', 'USD'),
       ('TSLA', 'Tesla', 'Tesla Inc.', 'BBG000N9MNX3', 'USD'),
       ('NVDA', 'Nvidia', 'Nvidia Inc.', 'BBG001S5N8V8', 'USD'),
       ('SAP', 'SAP','SAP SE', 'BBG000BB1CX2', 'EUR');

INSERT INTO crypto(symbol, name)
VALUES('BTC', 'Bitcoin'),
      ('ETH', 'Ethereum'),
      ('SOL', 'Solana'),
      ('DOGE', 'Dogecoin'),
      ('XRP', 'XRP');

INSERT INTO metal(symbol, name)
VALUES('XAG', 'Silber'),
      ('XAU', 'Gold'),
      ('XPT', 'Platin');

INSERT INTO finance_owner (id, name, mail)
VALUES (1, 'Alice Smith', 'alice.smith@example.com'),
       (2, 'Bob Johnson', 'bob.johnson@example.com'),
       (3, 'Charlie Brown', 'charlie.brown@example.com');

INSERT INTO stock_quote (
    current_price,
    change,
    percent_change,
    high_price_of_the_day,
    low_price_of_the_day,
    open_price_of_the_day,
    previous_close_price,
    time_stamp,
    stock_symbol
)
VALUES
    (182.50, 2.5, 1.39, 185.00, 20.00, 181.00, 180.00, '2023-12-13 10:30:00', 'AAPL'),
    (250.75, -1.25, -0.50, 255.00, 249.00, 252.00, 252.00, '2023-12-04 11:00:00', 'TSLA'),
    (132.20, 0.75, 0.57, 133.50, 131.00, 131.50, 131.00, '2023-12-13 12:15:00', 'NVDA'),
    (123.45, -0.55, -0.44, 124.00, 123.00, 123.80, 124.00, '2023-12-12 13:00:00', 'SAP'),
    (120.50, 2.5, 1.39, 220.00, 50.00, 420.00, 180.00, '2023-01-03 10:30:00', 'AAPL'),
    (190.50, 2.5, 1.39, 200.00, 180.00, 181.00, 180.00, '2023-12-13 10:30:00', 'AAPL'),
    (182.50, 2.5, 1.39, 185.00, 180.00, 181.00, 180.00, '2023-12-12 10:30:00', 'AAPL'),
    (182.50, 2.5, 1.39, 185.00, 180.00, 181.00, 180.00, '2023-12-15 10:45:00', 'AAPL');

INSERT INTO latest_stock_quote(
    stock_symbol,
    quote_id
)
VALUES
    ( 'AAPL', 1 ),
    ( 'TSLA', 2 ),
    ( 'NVDA', 3 ),
    ( 'SAP', 4 );

INSERT INTO crypto_quote(
    current_price,
    time_stamp,
    crypto_symbol
)
VALUES
    (1, '2023-12-13 10:30:00', 'BTC'),
    (1, '2023-12-13 10:30:00', 'ETH'),
    (1, '2023-12-13 10:30:00', 'SOL'),
    (1, '2023-12-13 10:30:00', 'DOGE'),
    (1, '2023-12-13 10:30:00', 'XRP');

INSERT INTO latest_crypto_quote(
    crypto_symbol,
    quote_id
)
VALUES
    ( 'BTC', 1 ),
    ( 'ETH', 2 ),
    ( 'SOL', 3 ),
    ( 'DOGE', 4 ),
    ( 'XRP', 5 );

INSERT INTO metal_quote(
    current_price,
    time_stamp,
    metal_symbol
)
VALUES
    (1, '2023-12-13 10:30:00', 'XAG'),
    (1, '2023-12-13 10:30:00', 'XAU'),
    (1, '2023-12-13 10:30:00', 'XPT');

INSERT INTO latest_metal_quote(
    metal_symbol,
    quote_id
)
VALUES
    ( 'XAG', 1 ),
    ( 'XAU', 2 ),
    ( 'XPT', 3 );

INSERT INTO bankaccount (currency, balance)
VALUES ('USD', 10000.50),
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
       (1, 'NVDA', 5.0),
       (2, 'SAP', 15.0),
       (3, 'AAPL', 25.0);

-- Order
INSERT INTO stock_order (purchase_amount, type, execution_timestamp, investment_account_id, stock_symbol)
VALUES
    (10000.0, 'BUY', '2025-01-18 09:00:00', 1, 'AAPL'), -- Order for 100 AAPL from Investmentkonto 1
    (50.0, 'SELL', '2025-01-18 09:30:00', 1, 'TSLA'), -- Order for 50 TSLA from Investmentkonto 1
    (25.5, 'BUY', '2025-01-18 10:00:00', 2, 'NVDA'), -- Order for 25.5 NVDA from Investmentkonto 2
    (10.0, 'SELL', '2025-01-18 10:30:00', 3, 'SAP'); -- Order for 10 SAP from Investmentkonto 3

-- Order for 10 SAP from Investmentkonto 3

INSERT INTO exchange (symbol, description)
VALUES ('NYSE', 'New York Stock Exchange'),
       ('NASDAQ', 'NASDAQ Stock Market'),
       ('LSE', 'London Stock Exchange');