export interface Order {
  id: number,
  volume: number,
  type: OrderType,
  investmentAccountId: number,
  stock: Stock,
  stockSymbol: String
}

export enum OrderType {
  BUY = 'BUY',
  SELL = 'SELL'
}

export interface Stock {
  symbol: string,
  description: string,
  figi: string,
  currency: Currency,
  currentValue: number,
  change: number,
  changePercentage: number
}

export interface StockDetails {
  symbol: string,
  description: string,
  figi: string,
  currency: Currency,
  currentValue: number,
  change: number,
  changePercentage: number,
  amount: number
}

export enum Currency {
  USD = 'USD',
  EUR = 'EUR'
}

export interface InvestmentAccount {
  id: number,
  bankAccountId: number,
  portfolio: PortfolioEntry[],
  userId: number,
  bankAccount: BankAccount,
  user: User
}

export interface PortfolioEntry {
  id: number,
  investmentAccountId: number,
  stockSymbol: String,
  quantity: number,
  stock: Stock,
  currentValue: number,
  amount: number,
  change: number,
  changePercentage: number
}

export interface BankAccount {
  id: number,
  currency: Currency,
  balance: number
}

export interface User {
  id: number,
  name: String,
  mail: String
}
