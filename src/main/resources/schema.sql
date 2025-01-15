DROP TABLE If EXISTS investment_portfolio;
DROP TABLE IF EXISTS portfolio_entry;
DROP TABLE IF EXISTS stock_order;
DROP TABLE IF EXISTS investmentaccount;
DROP TABLE IF EXISTS exchange;
DROP TABLE IF EXISTS bankaccount;
DROP TABLE IF EXISTS quote;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS crypto;
DROP TABLE IF EXISTS finance_owner;

---------------------------------------Stock-------------------------------------------------------

CREATE TABLE stock
(
    symbol      VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(50),
    description VARCHAR(255) NOT NULL,
    figi        VARCHAR(50)  NOT NULL,
    currency    VARCHAR(3)   NOT NULL, -- ISO 4217
    cprice      DECIMAL(18, 4)
);

---------------------------------------Crypto-------------------------------------------------------

CREATE TABLE crypto
(
    symbol      VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(50),
    cprice      DECIMAL(18, 4)
);

---------------------------------------Owner-------------------------------------------------------

CREATE TABLE finance_owner
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100)        NOT NULL,
    mail VARCHAR(255) UNIQUE NOT NULL
);

-------------------------------------------Quote---------------------------------------------------

CREATE TABLE quote
(
    id                    SERIAL PRIMARY KEY,
    current_price         DECIMAL(18, 4) NOT NULL,
    change                FLOAT          NOT NULL,
    percent_change        FLOAT          NOT NULL,
    high_price_of_the_day DECIMAL(18, 4) NOT NULL,
    low_price_of_the_day  DECIMAL(18, 4) NOT NULL,
    open_price_of_the_day DECIMAL(18, 4) NOT NULL,
    previous_close_price  DECIMAL(18, 4) NOT NULL,
    time_stamp            TIMESTAMP      NOT NULL,
    stock_symbol          VARCHAR(50)    NOT NULL,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

--------------------------------------------BankAccount--------------------------------------------------

CREATE TABLE bankaccount
(
    id       SERIAL PRIMARY KEY,
    currency VARCHAR(3)                    NOT NULL, -- ISO 4217
    balance  DECIMAL(18, 4) DEFAULT 0.0000 NOT NULL
);

--------------------------------InvestmentAccount--------------------------------------------------------------

CREATE TABLE investmentaccount
(
    id              SERIAL PRIMARY KEY,
    bank_account_id BIGINT,
    owner_id        BIGINT,
    FOREIGN KEY (bank_account_id) REFERENCES bankaccount (id) ON DELETE SET NULL,
    FOREIGN KEY (owner_id) REFERENCES finance_owner (id) ON DELETE SET NULL
);

CREATE TABLE portfolio_entry
(
    id           SERIAL PRIMARY KEY,
    investment_account_id BIGINT,
    stock_symbol VARCHAR(50) NOT NULL,
    quantity     FLOAT       NOT NULL,
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

-------------------------------------Order---------------------------------------------------------

-- Tabelle erstellen
CREATE TABLE stock_order
(
    id                 SERIAL PRIMARY KEY,
    volume             FLOAT       NOT NULL,
    type               VARCHAR(4)  NOT NULL CHECK (type IN ('BUY', 'SELL')),
    investment_account_id BIGINT      NOT NULL,
    stock_symbol       VARCHAR(50) NOT NULL,
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

--------------------------------------Exchange--------------------------------------------------------

CREATE TABLE exchange
(
    id          SERIAL PRIMARY KEY,
    symbol      VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

----------------------------------------------------------------------------------------------
