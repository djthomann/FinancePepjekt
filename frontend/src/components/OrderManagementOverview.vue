<template>
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>Ordermanagement-Übersicht</h1>
      </div>
    </div>

    <h2>Orders</h2>
    <table>
      <thead>
      <tr>
        <th>Id</th>
        <th>Volumen</th>
        <th>Typ</th>
        <th>Wertpapier-Symbol</th>
        <th>Wertpapier-Beschreibung</th>
        <th>Wertpapier-Figi</th>
        <th>Wertpapier-Währung</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="order in orders" :key="order.id" @click="navigateToStockDetail(order.stock.symbol)">
        <td>{{ order.id }}</td>
        <td>{{ order.volume }}</td>
        <td>{{ order.type }}</td>
        <td>{{ order.stock.symbol }}</td>
        <td>{{ order.stock.description }}</td>
        <td>{{ order.stock.figi }}</td>
        <td>{{ order.stock.currency }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {useRouter} from "vue-router";
import {Currency, OrderType} from '@/types/types.ts';
import type {Order} from '@/types/types.ts'

const router = useRouter()
const orders = ref<Order[]>([{
  id: 1,
  volume: 100.5,
  type: OrderType.BUY,
  stock: {
    symbol: 'AAPL',
    description: 'Apple Inc.',
    figi: 'BBG000B9XRY4',
    currency: Currency.USD
  }
},
  {
    id: 2,
    volume: 50.75,
    type: OrderType.SELL,
    stock: {
      symbol: 'GOOGL',
      description: 'Alphabet Inc.',
      figi: 'BBG009S39JX6',
      currency: Currency.USD
    }
  }
])

const navigateToStockDetail = (symbol: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol}});
}

</script>

<style lang="scss">
@use "./style.scss";
</style>


<style lang="scss">
@use "./style.scss";
</style>
