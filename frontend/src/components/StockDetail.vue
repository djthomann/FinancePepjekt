<template>
  <div class="stock-detail">
    <h2>{{ stock.symbol }} - Detailansicht</h2>
    <p><strong>Symbol:</strong> {{ stock.symbol }}</p>
    <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>
    <p><strong>Beschreibung:</strong></p>
    <p>{{ stock.description }}</p>

    <div class="purchase-buttons">
      <button class="purchase-button" @click="purchase(stock.symbol)">Kaufen</button>
      <button class="purchase-button" @click="sell(stock.symbol)">Verkaufen</button>
    </div>

    <div class="stock-info-section">
      <h2>Im Besitz</h2>
      <table>
        <tbody>
        <tr>
          <td>{{ stock.amount }} Stück</td>
          <td>{{ stock.currentValue * stock.amount }} €</td>
        </tr>
        <tr>
          <td>Seit Kauf</td>
          <td :class="{ 'positive': stock.change >= 0, 'negative': stock.change < 0 }">
            {{ stock.change }} € ({{ stock.changePercentage }}%)
          </td>
        </tr>
        <tr>
          <td>Kaufpreis gesamt</td>
          <td>{{ stock.currentValue * stock.amount }} €</td>
        </tr>
        <tr>
          <td>Kaufpreis Stück</td>
          <td>{{ stock.currentValue }} €</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="stock-info-section">
      <h2>Kurse</h2>
      <table>
        <tbody>
        <tr>
          <td>Tagestief</td>
          <td>{{ stock.currentValue }} €</td>
        </tr>
        <tr>
          <td>Tageshoch</td>
          <td>{{ stock.currentValue }} €</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {StockDetails} from "@/types/types.ts";

const route = useRoute();
const router = useRouter()
const symbol = route.params.symbol
const stock = ref<StockDetails>({})

function sell(symbol: string) {
  console.log('Verkaufen')
  router.push({name: 'order-management-sell', params: {symbol}});
}

function purchase(symbol: string) {
  console.log('Kaufen')
  router.push({name: 'order-management-buy', params: {symbol}});
}

onMounted(async () => {
  try {
    const response = await fetch(`/api/stock-details/symbol?symbol=${symbol}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stock.value = await response.json() as StockDetails
  } catch (e) {
    console.error(e)
  }
})

</script>

<style lang="scss">
@use "./style.scss";
</style>

