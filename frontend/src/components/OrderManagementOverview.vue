<template xmlns:th="http://www.w3.org/1999/xhtml">
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>Orderübersicht</h1>
      </div>
    </div>
    <table>
      <thead>
      <tr>
        <th>Id</th>
        <th>Einkaufswert</th>
        <th>Typ</th>
        <th>Symbol</th>
        <th>Beschreibung</th>
        <th>Figi</th>
        <th>Währung</th>
        <th>Ausführungszeitpunkt</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="order in orders" :key="order.id" @click="navigateToStockDetail(order.stock.symbol)">
        <td>{{ order.id }}</td>
        <td>{{ order.purchaseAmount }}</td>
        <td>{{ order.type }}</td>
        <td>{{ order.stock.symbol }}</td>
        <td>{{ order.stock.description }}</td>
        <td>{{ order.stock.figi }}</td>
        <td>{{ order.stock.currency }}</td>
        <td>{{ order.executionTimestamp }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useRouter, useRoute} from "vue-router";
import type {Order} from '@/types/types.ts'

const router = useRouter()
const route = useRoute()
const investmentAccountId = route.params.investmentAccountId
const orders = ref<Order[]>([])

onMounted(async () => {
  try {
    const response = await fetch(`/api/orders?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    orders.value = await response.json() as Order[]
  } catch (e) {
    console.error(e)
  }
})

const navigateToStockDetail = (symbol: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol}})
}

</script>

<style lang="scss">
@use "./style.scss";
</style>
