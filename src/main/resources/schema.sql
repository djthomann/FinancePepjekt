/*
-- Zuordnungstabellen zuerst löschen, da sie auf andere Tabellen verweisen
DROP TABLE IF EXISTS investment_portfolio;

-- Haupttabellen löschen
DROP TABLE IF EXISTS order;
DROP TABLE IF EXISTS investmentaccount;
DROP TABLE IF EXISTS exchange;
DROP TABLE IF EXISTS bankaccount;
DROP TABLE IF EXISTS quote;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS user;


 */
---------------------------------------User-------------------------------------------------------

CREATE TABLE stock
(
    symbol      VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    figi        VARCHAR(50)  NOT NULL,
    currency    VARCHAR(3)   NOT NULL -- ISO 4217-Währungsstandard
);
/*
-- Beispiel-Datensätze
INSERT INTO stock (symbol, description, figi, currency)
VALUES ('AAPL', 'Apple Inc.', 'BBG000B9XRY4', 'USD'),
       ('TSLA', 'Tesla Inc.', 'BBG000N9MNX3', 'USD'),
       ('GOOGL', 'Alphabet Inc.', 'BBG001S5N8V8', 'USD'),
       ('SAP', 'SAP SE', 'BBG000BB1CX2', 'EUR');


 */
---------------------------------------User-------------------------------------------------------
/*
CREATE TABLE user
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(100)        NOT NULL,
    mail VARCHAR(255) UNIQUE NOT NULL
);

-- Beispiel-Datensätze
INSERT INTO user (id, name, mail)
VALUES (1, 'Alice Smith', 'alice.smith@example.com'),
       (2, 'Bob Johnson', 'bob.johnson@example.com'),
       (3, 'Charlie Brown', 'charlie.brown@example.com');

-------------------------------------------Quote---------------------------------------------------

CREATE TABLE quote
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    value        DECIMAL(18, 4) NOT NULL,
    timeStamp    TIMESTAMP      NOT NULL,
    stock_symbol VARCHAR(50)    NOT NULL,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

-- Beispiel-Datensätze
INSERT INTO quote (value, timeStamp, stock_symbol)
VALUES (182.50, '2024-12-17 10:30:00', 'AAPL'),
       (250.75, '2024-12-17 11:00:00', 'TSLA'),
       (132.20, '2024-12-17 12:15:00', 'GOOGL'),
       (123.45, '2024-12-17 13:00:00', 'SAP');

--------------------------------------------BankAccount--------------------------------------------------

CREATE TABLE bankaccount
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    currency VARCHAR(3)                    NOT NULL, -- ISO 4217-Währungsstandard
    balance  DECIMAL(18, 4) DEFAULT 0.0000 NOT NULL
);

-- Beispiel-Datensätze
INSERT INTO bankaccount (currency, balance)
VALUES ('USD', 1000.50),
       ('EUR', 250.00),
       ('JPY', 500000.00),
       ('GBP', 123.45);

--------------------------------InvestmentAccount--------------------------------------------------------------

CREATE TABLE investmentaccount
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_account_id BIGINT,
    owner_id        BIGINT,
    FOREIGN KEY (bank_account_id) REFERENCES bankaccount (id) ON DELETE SET NULL,
    FOREIGN KEY (owner_id) REFERENCES user (id) ON DELETE SET NULL
);

CREATE TABLE investment_portfolio
(
    investment_account_id BIGINT      NOT NULL,
    stock_symbol          VARCHAR(50) NOT NULL,
    quantity              FLOAT       NOT NULL,
    PRIMARY KEY (investment_account_id, stock_symbol),
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

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

-------------------------------------Order---------------------------------------------------------

CREATE TABLE order
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    volume                FLOAT       NOT NULL,
    type                  ENUM('BUY', 'SELL') NOT NULL,
    investment_account_id BIGINT      NOT NULL,
    stock_symbol          VARCHAR(50) NOT NULL,
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

-- Beispiel-Datensätze
INSERT INTO order (volume, type, investment_account_id, stock_symbol)
VALUES (100.0, 'BUY', 1, 'AAPL'), -- Order für 100 AAPL von Investmentkonto 1
       (50.0, 'SELL', 1, 'TSLA'), -- Order für 50 TSLA von Investmentkonto 1
       (25.5, 'BUY', 2, 'GOOGL'), -- Order für 25.5 GOOGL von Investmentkonto 2
       (10.0, 'SELL', 3, 'SAP');
-- Order für 10 SAP von Investmentkonto 3

--------------------------------------Exchange--------------------------------------------------------

CREATE TABLE exchange
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol      VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

-- Beispiel-Datensätze
-- Börsen
INSERT INTO exchange (symbol, description)
VALUES ('NYSE', 'New York Stock Exchange'),
       ('NASDAQ', 'NASDAQ Stock Market'),
       ('LSE', 'London Stock Exchange');

----------------------------------------------------------------------------------------------
*/

