<template>
  <div class="invest-depot">
      <div>
            <h1>Wertpapiere</h1>
            <div id="searchField">
                  <input v-model="searchField" placeholder="Symbol/Name" />
                  <button class="details-button" @click="resetSearch">Reset</button>
                  <button class="details-button" @click="searchContent">Search</button>
                  <button class="details-button" @click="poll">Aktualisieren</button>
            </div>
      </div>

      <div>
            <table>
                  <thead>
                        <tr>
                              <th>Name</th>
                              <th>Symbol</th>
                              <th>Aktueller Wert</th>
                              <th>Währung</th>
                              <th>Gewinn/Verlust</th>
                        </tr>
                  </thead>
                  <tbody>
                        <tr class="table-row" v-for="stock in stocks" :key="stock.id" :class="{ 'just-changed': stock.justChanged}" @click="navigateToStockDetail(stock.symbol)">
                              <td>{{ stock.name }}</td>
                              <td>{{ stock.symbol }}</td>
                              <td>{{ stock.cprice }}</td>
                              <td>{{stock.currency}}</td>
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
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";
import type { Stock} from "@/types/types.ts";

const router = useRouter()
const searchField = ref('')
/*const stocks = ref([
  { id: 1, name: 'Apple', symbol: "AAPL", currentValue: 1450.90, change: 33.39, changePercentage: 13.6 },
  { id: 2, name: 'Tesla', symbol: "TSLA", currentValue: 3565.35, change: 120.75, changePercentage: 20.1 },
  { id: 3, name: 'Amazon', symbol: "GOOGL", currentValue: 6169.52, change: -45.50, changePercentage: -5.2 },
])
*/
const stocks = ref<Stock[]>([])

setInterval(poll, 3000)

function resetSearch() {
  searchField.value = ''
}

async function poll() {

  console.log("polling")

  for (const stock of stocks.value) {
    try {
      const response = await fetch(`/api/stock/by/symbol?symbol=${stock.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      // Aktualisiere stocks.value mit den abgerufenen Daten
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

onMounted(async () => {
  try {
    const response = await fetch(`/api/stocks`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stocks.value = await response.json() as Stock[]
  } catch (e) {
    console.error(e)
  }
})

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
