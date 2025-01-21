export interface Order {
  id?: number,
  purchaseAmount: number,
  type: OrderType,
  investmentAccountId: number,
  executionTimestamp: string,
  stock: Stock,
  stockSymbol: string
}

export enum OrderType {
  BUY = 'BUY',
  SELL = 'SELL'
}


export interface Stock {
  symbol: string,
  name: string,
  description: string,
  figi: string,
  currency: Currency,
  change: number,
  changePercentage: number,
  latestQuote: Quote,

  justChanged: boolean // Just for frontend
}

export interface StockDetails {
  stock: Stock,
  portfolioEntry: PortfolioEntry,
}

export interface Metal {
  symbol: string,
  name: string,
  currentPrice: number,

  justChanged: boolean // Just for frontend
}

export enum Currency {
  USD = 'USD',
  EUR = 'EUR'
}

export interface InvestmentAccount {
  id: number,
  bankAccountId: number,
  portfolio: PortfolioEntry[],
  totalValue: number,
  bankAccount: BankAccount,
  owner: Owner
}

export interface Quote {
  currentPrice: number,
  change: number,
  percentChange: number,
  highPriceOfTheDay: number,
  lowPriceOfTheDay: number,
  openPriceOfTheDay: number,
  previousClosePrice: number,
  timeStamp: string,
  stockSymbol: string
}

export interface Coin {
  symbol: string;
  name: string,
  description: string;
  currency: Currency;
  currentPrice: number;

  justChanged: boolean //Just for frontend
}

export interface PortfolioEntry {
  id: number,
  investmentAccountId: number,
  stockSymbol: string,
  quantity: number,
  stock: Stock,
  totalValue: number,
  profitAndLoss: number,
  profitAndLossPercent: number
}

export interface BankAccount {
  id: number,
  currency: Currency,
  balance: number
}

export interface Owner {
  id: number,
  name: string,
  mail: string
}
