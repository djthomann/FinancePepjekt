<template>
  <div class="invest-depot">
    <div>
      <h1>Wertpapiere</h1>
      <div><!--Leeres Element für Grid--></div>
      <div id="searchField">
        <input v-model="search" placeholder="Symbol/Name"/>
        <button class="details-button" @click="resetSearch">Reset</button>
      </div>
    </div>

    <div v-if="favoriteStocks.length != 0">
      <h3>Deine Favoriten</h3>
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

        <tr class="table-row" v-for="favStock in favoriteStocks" :key="favStock.figi" :class="{ 'just-changed':
                       favStock.justChanged}" @click="navigateToStockDetail(favStock.symbol, investmentAccountId)">
          <td>{{ favStock.name }}</td>
          <td>{{ favStock.symbol }}</td>
          <td>{{ favStock.currency }}</td>
          <td>{{ favStock.latestQuote.currentPrice }}</td>
          <td :class="{ 'positive': favStock.latestQuote.change >= 0, 'negative': favStock.latestQuote.change < 0 }">
            {{ favStock.latestQuote.change }} ({{ favStock.latestQuote.percentChange }}%)
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div>
      <h3>Wertpapiere</h3>
      <table>
        <thead>
        <tr>
          <th>
            <button :class="{ 'sorting-button-down': nameDescending}" class="sorting-button" @click="sortByName">Name
            </button>
          </th>
          <th>Symbol</th>
          <th>Währung</th>
          <th>
            <button :class="{ 'sorting-button-down': priceDescending}" class="sorting-button" @click="sortByPrice">
              Aktueller Wert
            </button>
          </th>
          <th>Heutige Bilanz</th>
          <th></th>
        </tr>
        </thead>
        <tbody>

        <tr class="table-row" v-for="stock in filteredStocks" :key="stock.figi" :class="{ 'just-changed':
                       stock.justChanged}" @click="navigateToStockDetail(stock.symbol, investmentAccountId)">
          <td>{{ stock.name }}</td>
          <td>{{ stock.symbol }}</td>
          <td>{{ stock.currency }}</td>
          <td>{{ stock.latestQuote.currentPrice }}</td>
          <td :class="{ 'positive': stock.latestQuote.change >= 0, 'negative': stock.latestQuote.change < 0 }">
            {{ stock.latestQuote.change }} ({{ stock.latestQuote.percentChange }}%)
          </td>
          <td>
            <img
              @mouseover="hoverFavorite(stock)"
              @mouseleave="leaveFavorite(stock)"
              @click.stop="toggleFavorite(stock)"
              :src="stock.tempFavoriteIcon || (stock.isFavorite ? '/noFavorite.png' : '/favorite.png')"
              :alt="stock.isFavorite ? 'Favorit entfernen' : 'Zu Favoriten hinzufügen'"
              class="favorite-icon"
            >
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRoute, useRouter} from "vue-router"
import type {Stock} from "@/types/types.ts"

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
)
const favoriteStocks = ref<Stock[]>([])

function hoverFavorite(stock: Stock) {
  stock.tempFavoriteIcon = stock.isFavorite ? '/favorite.png' : '/noFavorite.png'
}

function leaveFavorite(stock: Stock) {
  stock.tempFavoriteIcon = ""
}

async function poll() {
  // stocks
  for (const stock of stocks.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}&investmentAccountId=${investmentAccountId}`)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const stockData = await response.json() as Stock

      if (stock.latestQuote.currentPrice !== stockData.latestQuote.currentPrice) {
        stock.latestQuote.currentPrice = stockData.latestQuote.currentPrice
        stock.latestQuote.percentChange = stockData.latestQuote.percentChange
        stock.latestQuote.change = stockData.latestQuote.change
        stock.justChanged = true

        setTimeout(() => {
          stock.justChanged = false
        }, 200)
      }
    } catch (e) {
      console.error(e)
    }
  }
}

async function pollFavoriteStocks() {
  if (investmentAccountId) {
    try {
      const response = await fetch(`/api/favorites?investmentAccountId=${investmentAccountId}`)
      const newFavorites = await response.json() as Stock[]

      // Remove favourites that are no longer included in the response
      favoriteStocks.value = favoriteStocks.value.filter(favoriteStock => newFavorites.includes(favoriteStock))
      // Update stocks
      for (const stock of newFavorites) {
        const existingStock = favoriteStocks.value.find(oldFavoriteStock => oldFavoriteStock.symbol === stock.symbol)
        const stockResponse = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}&investmentAccountId=${investmentAccountId}`)
        const stockData = await stockResponse.json() as Stock

        if (existingStock) {
          const index = favoriteStocks.value.indexOf(existingStock)
          favoriteStocks.value[index] = stockData
        } else {
          favoriteStocks.value.push(stockData)
        }
      }
    } catch (error) {
      console.error('Fehler beim Abrufen der Favoriten:', error)
    }
  }
}

function sortByPrice() {
  priceDescending.value = !priceDescending.value
  if (nameDescending.value == true) {
    nameDescending.value = false
  }
  console.log(priceDescending.value)
  if (priceDescending.value) {
    stocks.value = [...stocks.value].sort((a, b) => a.latestQuote.currentPrice - b.latestQuote.currentPrice)
  } else {
    stocks.value = [...stocks.value].sort((a, b) => b.latestQuote.currentPrice - a.latestQuote.currentPrice)
  }

}

function sortByName() {
  nameDescending.value = !nameDescending.value
  if (priceDescending.value == true) {
    priceDescending.value = false
  }
  if (nameDescending.value) {
    stocks.value = [...stocks.value].sort((a, b) => a.name.localeCompare(b.name))
  } else {
    stocks.value = [...stocks.value].sort((a, b) => b.name.localeCompare(a.name))
  }
}

onMounted(async () => {
  // stocks
  try {
    const response = await fetch(`/api/stocks?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stocks.value = await response.json() as Stock[]
  } catch (e) {
    console.error(e)
  }

  //favs
  try {
    const response = await fetch(`/api/favorites?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    favoriteStocks.value = await response.json() as Stock[]
  } catch (e) {
    console.error(e)
  }

  sortByName()
  pollingIntervalID = setInterval(poll, 3000)
  pollingIntervalID = setInterval(pollFavoriteStocks, 3000)
})

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

function resetSearch() {
  search.value = ''
}

const navigateToStockDetail = (symbol: string, investmentAccountId: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol, investmentAccountId}})
}

async function toggleFavorite(stock: Stock) {
  try {
    let url = ""
    if(stock.isFavorite){
      url = `/api/delete-favorites?investmentAccountId=${investmentAccountId}&stockSymbol=${stock.symbol}`
    } else {
      url = `/api/add-favorites?investmentAccountId=${investmentAccountId}&stockSymbol=${stock.symbol}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        investmentAccountId: investmentAccountId,
        stockSymbol: stock.symbol
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    stock.isFavorite = !stock.isFavorite
  } catch (error) {
    console.error('Error toggling favorite:', error)
  }
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

.favorite-icon {
  width: 24px;
  height: 24px;
  cursor: pointer;
}

.favorite-icon:hover {
  opacity: 0.8;
  transform: scale(1.3);
}

</style>
