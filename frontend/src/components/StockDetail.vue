<template>
  <div class="stock-detail">
    <h2>{{ stock.name }} - Detailansicht</h2>
    <p><strong>Symbol:</strong> {{ stock.symbol }}</p>
    <p><strong>FIGI:</strong> {{ stock.figi }}</p>
    <p :class="{ 'just-changed': stock.justChanged}"><strong>Aktueller Wert:</strong> {{ stock.cprice }} {{stock.currency}}</p>
    <p><strong>Beschreibung:</strong> {{ stock.description }}</p>

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
import {onBeforeMount, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type { Stock} from '@/types/types.ts'

const stock = ref<Stock>({})
let pollingIntervalID: number

async function poll() {

  console.log("polling")

  try {
    const response = await fetch(`/api/stock/by/symbol?symbol=${stock.value.symbol}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const stockData = await response.json() as Stock;
    if(stock.value.cprice !== stockData.cprice) {
      stock.value.cprice = stockData.cprice
      stock.value.justChanged = true

      setTimeout(() => {
        stock.value.justChanged = false;
      }, 200);
    }
  } catch (e) {
    console.error(e);
  }

}

onBeforeMount(async () => {

  const route = useRoute();

  const symbol = route.params.symbol;
  console.log("Symbol", symbol)

  try {
    const response = await fetch(`/api/stock-details/symbol?symbol=${symbol}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stock.value = await response.json() as Stock
    console.log(stock.value)
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)
})

onUnmounted( () => {
  console.log("Cleaing interval for polling")
  clearInterval(pollingIntervalID)
})

const router = useRouter()

function sell(symbol: string) {
  console.log('Verkaufen')
  router.push({name: 'order-management-sell', params: {symbol}});
}

function purchase(symbol: string) {
  console.log('Kaufen')
  router.push({name: 'order-management-buy', params: {symbol}});
}

</script>

<style lang="scss">
@use "./style.scss";
.just-changed {
  background-color: var(--main-color-light);
}
</style>

