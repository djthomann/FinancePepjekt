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
import {onMounted, ref} from 'vue';
import {useRouter, useRoute} from "vue-router";
import type {Order, Stock} from '@/types/types.ts'

const router = useRouter()
const route = useRoute();
const investmentAccountId = route.params.investmentAccountId
const orders = ref<Order[]>([])

onMounted(async () => {
  try {
    const response = await fetch(`/api/orders?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    orders.value = await response.json() as Order[]

    for (let order of orders.value) {
      try {
        const stockResponse = await fetch(`/api/stock/by/symbol?symbol=${order.stockSymbol}`)
        if (!stockResponse.ok) {
          throw new Error(`HTTP error! status: ${stockResponse.status}`)
        }
        const stockData = await stockResponse.json() as Stock
        order.stock = {...order.stock, ...stockData}
      } catch (error) {
        console.error(`Error fetching stock data for ${order.stock.symbol}:`, error)
      }
    }
    console.log("Final orders data:", orders.value)

  } catch (error) {
    console.error('Fehler beim Abrufen der Orders:', error);
  }
});


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
