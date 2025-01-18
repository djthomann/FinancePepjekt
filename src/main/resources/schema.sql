DROP TABLE If EXISTS investment_portfolio;
DROP TABLE IF EXISTS portfolio_entry;
DROP TABLE IF EXISTS stock_order;
DROP TABLE IF EXISTS investmentaccount;
DROP TABLE IF EXISTS exchange;
DROP TABLE IF EXISTS bankaccount;
DROP TABLE IF EXISTS latest_stock_quote;
DROP TABLE IF EXISTS stock_quote;
DROP TABLE IF EXISTS latest_crypto_quote;
DROP TABLE IF EXISTS crypto_quote;
DROP TABLE IF EXISTS latest_metal_quote;
DROP TABLE IF EXISTS metal_quote;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS crypto;
DROP TABLE IF EXISTS metal;
DROP TABLE IF EXISTS finance_owner;

---------------------------------------Stock-------------------------------------------------------

CREATE TABLE stock
(
    symbol        VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(50),
    description   VARCHAR(255) NOT NULL,
    figi          VARCHAR(50)  NOT NULL,
    currency      VARCHAR(3)   NOT NULL, -- ISO 4217
    current_price DECIMAL(18, 4)
);

---------------------------------------Crypto-------------------------------------------------------

CREATE TABLE crypto
(
    symbol        VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(50),
    current_price DECIMAL(18, 4)
);

---------------------------------------Metals-------------------------------------------------------

CREATE TABLE metal
(
    symbol        VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(50),
    current_price DECIMAL(18, 4)
);

---------------------------------------Owner-------------------------------------------------------

CREATE TABLE finance_owner
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100)        NOT NULL,
    mail VARCHAR(255) UNIQUE NOT NULL
);

-------------------------------------------Stock Quotes---------------------------------------------------

CREATE TABLE stock_quote
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

-------------------------------------------Latest Stock Quote---------------------------------------------------

CREATE TABLE latest_stock_quote
(
    stock_symbol VARCHAR(50) PRIMARY KEY,
    quote_id     BIGINT UNIQUE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE,
    FOREIGN KEY (quote_id) REFERENCES stock_quote (id) ON DELETE CASCADE
);


-------------------------------------------Crypto Quotes---------------------------------------------------

CREATE TABLE crypto_quote
(
    id            SERIAL PRIMARY KEY,
    current_price DECIMAL(18, 4) NOT NULL,
    time_stamp    TIMESTAMP      NOT NULL,
    crypto_symbol VARCHAR(50)    NOT NULL,
    FOREIGN KEY (crypto_symbol) REFERENCES crypto (symbol) ON DELETE CASCADE
);

-------------------------------------------Latest Crypto Quote---------------------------------------------------

CREATE TABLE latest_crypto_quote
(
    crypto_symbol VARCHAR(50) PRIMARY KEY,
    quote_id      BIGINT UNIQUE,
    FOREIGN KEY (crypto_symbol) REFERENCES crypto (symbol) ON DELETE CASCADE,
    FOREIGN KEY (quote_id) REFERENCES crypto_quote (id) ON DELETE CASCADE
);

-------------------------------------------Metal Quotes---------------------------------------------------

CREATE TABLE metal_quote
(
    id            SERIAL PRIMARY KEY,
    current_price DECIMAL(18, 4) NOT NULL,
    time_stamp    TIMESTAMP      NOT NULL,
    metal_symbol  VARCHAR(50)    NOT NULL,
    FOREIGN KEY (metal_symbol) REFERENCES metal (symbol) ON DELETE CASCADE
);

-------------------------------------------Latest Metal Quotes---------------------------------------------------

CREATE TABLE latest_metal_quote
(
    metal_symbol VARCHAR(50) PRIMARY KEY,
    quote_id     BIGINT UNIQUE,
    FOREIGN KEY (metal_symbol) REFERENCES metal (symbol) ON DELETE CASCADE,
    FOREIGN KEY (quote_id) REFERENCES metal_quote (id) ON DELETE CASCADE
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
    id                    SERIAL PRIMARY KEY,
    investment_account_id BIGINT,
    stock_symbol          VARCHAR(50) NOT NULL,
    quantity              FLOAT       NOT NULL,
    FOREIGN KEY (investment_account_id) REFERENCES investmentaccount (id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stock (symbol) ON DELETE CASCADE
);

-------------------------------------Order---------------------------------------------------------

CREATE TABLE stock_order
(
    id                    SERIAL PRIMARY KEY,
    purchase_amount       FLOAT       NOT NULL,
    type                  VARCHAR(4)  NOT NULL CHECK (type IN ('BUY', 'SELL')),
    execution_timestamp   TIMESTAMP   NOT NULL,
    investment_account_id BIGINT      NOT NULL,
    stock_symbol          VARCHAR(50) NOT NULL,
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
