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
          <th>FIGI</th>
          <th>Aktueller Wert</th>
          <th>Gewinn/Verlust</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="position in stocks" :key="position.symbol" @click="navigateToStockDetail(position.symbol)">
          <td>{{ position.description }}</td>
          <td>{{ position.symbol }}</td>
          <td>{{ position.figi }}</td>
          <td>{{ position.currentValue }} €</td>    <!--reactive value-->
          <td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
            {{ position.change }} € ({{ position.changePercentage }}%)
          </td>    <!--reactive value-->
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";
import type {Stock} from "@/types/types.ts";

const router = useRouter()
const searchField = ref('')

const stocks = ref<Stock[]>([])

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
</style>
