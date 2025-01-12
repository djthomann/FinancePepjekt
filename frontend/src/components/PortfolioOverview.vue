<template>
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>InvestDepot</h1>
        <p>Hans Mustermann</p>
      </div>
      <div class="total-value">
        <p>Depotwert: <strong>{{ totalValue }} €</strong></p>
      </div>
    </div>

    <h2>Positionen</h2>
    <table>
      <thead>
      <tr>
        <th>Name</th>
        <th>Symbol</th>
        <th>Aktueller Wert</th>
        <th>Anteile</th>
        <th>Gewinn/Verlust</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="position in positions" :key="position.id" @click="navigateToStockDetail(position.symbol)">
        <td>{{ position.name }}</td>
        <td>{{ position.symbol }}</td>
        <td>{{ position.currentValue }} €</td>
        <td>{{ position.amount }}</td>
        <td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
          {{ position.change }} € ({{ position.changePercentage }}%)
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {Stock} from "@/types/types.ts";

const router = useRouter()
const id = 1
const totalValue = ref(2345.56)
const positions = ref([
  { id: 1, name: 'Apple', symbol: "APPL", amount: 3, currentValue: 1450.90, change: 33.39, changePercentage: 13.6 },
  { id: 2, name: 'Tesla', symbol: "TSLA", amount: 3, currentValue: 3565.35, change: 120.75, changePercentage: 20.1 },
  { id: 3, name: 'Amazon', symbol: "AMZ", amount: 5, currentValue: 6169.52, change: -45.50, changePercentage: -5.2 },
])

onBeforeMount(async () => {

  try {
    const response = await fetch(`/api/portfolio?userId=${id}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    console.log(response.json())
  } catch (e) {
    console.error(e)
  }
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
</style>
