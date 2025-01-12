export interface Order {
  id?: number;
  volume: number;
  type: OrderType;
  stock: Stock;
  stockSymbol: string
}

export enum OrderType {
  BUY = 'BUY',
  SELL = 'SELL'
}

export interface Stock {
  symbol: string;
  name: string,
  description: string;
  figi: string;
  currency: Currency;
  cprice: number;
  justChanged: boolean
}

export interface PortfolioEntry {
  investmentAccountId: number,
  stockSymbol: string,
  quantity: number
}

export interface UserInfo {
  id: number,
  name: string,
  mail: string
}

export enum Currency {
  USD = 'USD'
}
