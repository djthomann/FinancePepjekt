-- Zuordnungstabellen zuerst löschen, da sie auf andere Tabellen verweisen
DROP TABLE IF EXISTS investment_portfolio;

-- Haupttabellen löschen
DROP TABLE IF EXISTS stock_order;
DROP TABLE IF EXISTS investmentaccount;
DROP TABLE IF EXISTS exchange;
DROP TABLE IF EXISTS bankaccount;
DROP TABLE IF EXISTS quote;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS finance_user;

---------------------------------------User-------------------------------------------------------

CREATE TABLE stock
(
    symbol      VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    figi        VARCHAR(50)  NOT NULL,
    currency    VARCHAR(3)   NOT NULL -- ISO 4217-Währungsstandard
);

---------------------------------------User-------------------------------------------------------

CREATE TABLE finance_user
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(100)        NOT NULL,
    mail VARCHAR(255) UNIQUE NOT NULL
);

-------------------------------------------Quote---------------------------------------------------

CREATE TABLE quote
(
    id           BIGINT PRIMARY KEY,
    value        DECIMAL(18, 4) NOT NULL,
    timeStamp    TIMESTAMP      NOT NULL,
    stock_symbol VARCHAR(50)    NOT NULL,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

--------------------------------------------BankAccount--------------------------------------------------

CREATE TABLE bankaccount
(
    id       BIGINT PRIMARY KEY,
    currency VARCHAR(3)                    NOT NULL, -- ISO 4217-Währungsstandard
    balance  DECIMAL(18, 4) DEFAULT 0.0000 NOT NULL
);

--------------------------------InvestmentAccount--------------------------------------------------------------

CREATE TABLE investmentaccount
(
    id              BIGINT PRIMARY KEY ,
    bank_account_id BIGINT,
    owner_id        BIGINT,
    FOREIGN KEY (bank_account_id) REFERENCES bankaccount (id) ON DELETE SET NULL,
    FOREIGN KEY (owner_id) REFERENCES finance_user (id) ON DELETE SET NULL
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

-------------------------------------Order---------------------------------------------------------

-- Tabelle erstellen
CREATE TABLE stock_order
(
    id                    BIGINT PRIMARY KEY,
    volume                FLOAT       NOT NULL,
    type                  VARCHAR(4)  NOT NULL CHECK (type IN ('BUY', 'SELL')),
    investment_account_id BIGINT      NOT NULL,
    stock_symbol          VARCHAR(50) NOT NULL,
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

--------------------------------------Exchange--------------------------------------------------------

CREATE TABLE exchange
(
    id          BIGINT PRIMARY KEY,
    symbol      VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

----------------------------------------------------------------------------------------------
