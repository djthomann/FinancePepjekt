export interface Order {
  id: number,
  volume: number,
  type: OrderType,
  investmentAccountId: number,
  stock: Stock,
  stockSymbol: string
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
  totalValue: number,
  bankAccount: BankAccount,
  owner: Owner
}

export interface PortfolioEntry {
  id: number,
  stockSymbol: string,
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

export interface Owner {
  id: number,
  name: string,
  mail: string
}
