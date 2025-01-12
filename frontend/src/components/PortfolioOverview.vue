<template>
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>InvestDepot</h1>
        <p>{{ username }}</p>
      </div>
      <div class="total-value">
        <p>Depotwert: <strong>{{ totalPortfolioValue }} €</strong></p>
      </div>
    </div>

    <h2>Positionen</h2>
    <table>
      <thead>
      <tr>
        <th>Name</th>
        <th>Symbol</th>
        <th>Anteile</th>
        <th>Aktueller Wert</th>
        <th>Gesamtwert</th>
      </tr>
      </thead>
      <tbody>
      <tr class="table-row" :class="{ 'just-changed': stock.justChanged}" v-for="stock in stocks" :key="stock.symbol" @click="navigateToStockDetail(stock.symbol)">
        <td>{{ stock.name }}</td>
        <td>{{ stock.symbol }}</td>
        <td>{{positions.get(stock.symbol)}}</td>
        <td>{{ stock.cprice }}</td>
        <!--<td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
          {{ position.change }} € ({{ position.changePercentage }}%)
        </td>-->
        <td>{{ stockValueMap.get(stock.symbol) }} </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {computed, onBeforeMount, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {PortfolioEntry, Stock, UserInfo} from "@/types/types.ts";

const router = useRouter()
const route = useRoute();
const totalValue = ref(2345.56)
let pollingIntervalID: number
const investmentAccountId = route.params.investmentAccountId
const username = ref<string>()

const stocks = ref<Stock[]>([])
const portfolio = ref<PortfolioEntry[]>([])
const positions = ref<Map<string, number>>(new Map());
let stockValueMap = <Map<string, computed<number>>>(new Map)
let totalPortfolioValue = computed<number>(0)

onBeforeMount(async () => {

  try {
    const response = await fetch(`/api/portfolio?userId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    portfolio.value = await response.json() as PortfolioEntry
    loadStocks()
    loadUserInfo()
    loadPositions()
    calculateStockValueMap()
    calculatePortfolioValue()
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)
})

function calculatePortfolioValue() {
  totalPortfolioValue = computed(() => {
    let totalValue = 0;

    stockValueMap.value.forEach(valueComputed => {
      totalValue += valueComputed.value;
    });

    return totalValue;
  });
}

async function loadUserInfo() {
  try {
    const response = await fetch(`/api/user?userId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json() as UserInfo
    username.value = data.name
  } catch (e) {
    console.error(e)
  }
}

function calculateStockValueMap() {

  stockValueMap = computed(() => {
    const map = new Map<string, computed<number>>();

    stocks.value.forEach(stock => {
      const quantity = positions.value.get(stock.symbol) || 0;
      const totalValue = computed(() => stock.cprice * quantity);  // Dynamischer Wert in computed
      map.set(stock.symbol, totalValue);  // Füge das computed Objekt in die Map ein
    });

    return map;
  });
}

function loadPositions() {

  for (const position of portfolio.value) {
    positions.value.set(position.stockSymbol, position.quantity)
  }

}

async function loadStocks() {

  if(stocks.value.length > 0) {
    return
  }

  console.log("loading Stocks")

  for (const position of portfolio.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${position.stockSymbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const stockData = await response.json() as Stock;
      stocks.value.push(stockData as Stock)
    } catch (e) {
      console.error(e);
    }
  }

  console.log(stocks.value)

}

async function poll() {

  console.log("polling")

  for (const stock of stocks.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const stockData = await response.json() as Stock;
      if(stock.cprice !== stockData.cprice) {
        stock.cprice = stockData.cprice
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

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const navigateToStockDetail = (symbol: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol}});
}

</script>

<style lang="scss">
@use "./style.scss";
</style>


<style lang="scss">
@use "./style.scss";

.just-changed {
  background-color: var(--main-color-light);
}

.table-row {
  transition: background-color 200ms;
}
</style>
