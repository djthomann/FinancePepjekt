<template>
  <div class="invest-depot">
    <div>
      <h1>Wertpapiere</h1>
      <div id="searchField">
        <input v-model="search" placeholder="Symbol/Name"/>
        <button class="details-button" @click="resetSearch">Reset</button>
      </div>
    </div>

    <div>
      <table>
        <thead>
        <tr>
          <th><button :class="{ 'sorting-button-down': nameDescending}" class="sorting-button" @click="sortByName">Name</button></th>
          <th>Symbol</th>
          <th>Währung</th>
          <th><button :class="{ 'sorting-button-down': priceDescending}" class="sorting-button" @click="sortByPrice">Aktueller Wert</button></th>
          <th>Gewinn/Verlust</th>
        </tr>
        </thead>
        <tbody>

        <tr class="table-row" v-for="stock in filteredStocks" :key="stock.figi" :class="{ 'just-changed':
                       stock.justChanged}" @click="navigateToStockDetail(stock.symbol, investmentAccountId)">
          <td>{{ stock.name }}</td>
          <td>{{ stock.symbol }}</td>
          <td>{{ stock.currency }}</td>
          <td>{{ stock.latestQuote.currentPrice }}</td>
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
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {InvestmentAccount, Stock} from "@/types/types.ts";

const router = useRouter()
const route = useRoute()
let pollingIntervalID: number
const investmentAccountId = route.params.investmentAccountId as string

const priceDescending = ref<boolean>(false)
const nameDescending = ref<boolean>(false)

const search = ref('')
const stocks = ref<Stock[]>([])
const filteredStocks = computed(() =>
  stocks.value.filter(stock => {
    return stock.symbol.toLowerCase().includes(search.value.toLowerCase()) || stock.name.toLowerCase().includes(search.value.toLowerCase())
  })
);

async function poll() {

  console.log("polling")

  for (const stock of stocks.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const stockData = await response.json() as Stock;

      if (stock.latestQuote.currentPrice !== stockData.latestQuote.currentPrice) {
        stock.latestQuote.currentPrice = stockData.latestQuote.currentPrice
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

function sortByPrice() {
  priceDescending.value = !priceDescending.value
  if(nameDescending.value == true) {
    nameDescending.value = false
  }
  console.log(priceDescending.value)
  if(priceDescending.value) {
    stocks.value = [...stocks.value].sort((a, b) => a.latestQuote.currentPrice - b.latestQuote.currentPrice)
  } else {
    stocks.value = [...stocks.value].sort((a, b) => b.latestQuote.currentPrice - a.latestQuote.currentPrice)
  }

}

function sortByName() {
  nameDescending.value = !nameDescending.value
  if(priceDescending.value == true) {
    priceDescending.value = false
  }
  if(nameDescending.value) {
    stocks.value = [...stocks.value].sort((a, b) => a.name.localeCompare(b.name));
  } else {
    stocks.value = [...stocks.value].sort((a, b) => b.name.localeCompare(a.name));
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

  sortByName()
  pollingIntervalID = setInterval(poll, 3000)
})

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

function resetSearch() {
  search.value = ''
}

const navigateToStockDetail = (symbol: string, investmentAccountId: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol, investmentAccountId}});
}

</script>

<style lang="scss">
@use "./style.scss";
</style>
