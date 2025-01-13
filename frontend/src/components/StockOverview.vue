<template>
  <div class="invest-depot">
    <div>
      <h1>Wertpapiere</h1>
      <div id="searchField">
        <input v-model="searchField" placeholder="Symbol/Name"/>
        <button class="details-button" @click="resetSearch">Reset</button>
        <button class="details-button" @click="searchContent">Search</button>
      </div>
    </div>

    <div>
      <table>
        <thead>
        <tr>
          <th>Name</th>
          <th>Symbol</th>
          <th>Währung</th>
          <th>Aktueller Wert</th>
          <th>Gewinn/Verlust</th>
        </tr>
        </thead>
        <tbody>

        <tr class="table-row" v-for="stock in stocks" :key="stock.figi" :class="{ 'just-changed':
                       stock.justChanged}" @click="navigateToStockDetail(stock.symbol)">
          <td>{{ stock.name }}</td>
          <td>{{ stock.symbol }}</td>
          <td>{{ stock.currency }}</td>
          <td>{{ stock.currentValue }}</td>
          <td :class="{ 'positive': stock.change >= 0, 'negative': stock.change < 0 }">
            {{ stock.change }} € ({{ stock.changePercentage }}%)
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, onUnmounted, ref} from 'vue';
import {useRouter} from "vue-router";
import type {Stock} from "@/types/types.ts";

const router = useRouter()
let pollingIntervalID: number
const searchField = ref('')

const stocks = ref<Stock[]>([])


async function poll() {

  console.log("polling")

  for (const stock of stocks.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const stockData = await response.json() as Stock;

      if (stock.currentValue !== stockData.currentValue) {
        stock.currentValue = stockData.currentValue
        stock.justChanged = true

        setTimeout(() => {
          stock.justChanged = false;
        }, 200);
      }
    } catch (e) {
      console.error(e);
    }
  }

}

onMounted(async () => {
  try {
    const response = await fetch("/api/stocks")
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stocks.value = await response.json() as Stock[]
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)
})

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

function resetSearch() {
  searchField.value = ''
}

function searchContent() {
  console.log('searching for:', searchField.value)
}

const navigateToStockDetail = (symbol: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol}});
}

</script>

<style lang="scss">
@use "./style.scss";

.just-changed {
  background-color: var(--main-color-light);
}

.table-row {
  transition: background-color 200ms;
}
</style>
