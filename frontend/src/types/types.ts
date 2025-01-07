export interface Order {
  id?: number;
  volume: number;
  type: OrderType;
  stock: Stock;
  stockSymbol: String
}

export enum OrderType {
  BUY = 'BUY',
  SELL = 'SELL'
}

export interface Stock {
  symbol: string;
  description: string;
  figi: string;
  currency: Currency;
}

export enum Currency {
  USD = 'USD'
}
