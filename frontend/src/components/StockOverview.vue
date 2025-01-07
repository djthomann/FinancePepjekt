<template>
  <div class="invest-depot">
      <div>
            <h1>Wertpapiere</h1>
            <div id="searchField">
                  <input v-model="searchField" placeholder="Symbol/Name" />
                  <button class="details-button" @click="resetSearch">Reset</button>
                  <button class="details-button" @click="searchContent">Search</button>
            </div>
      </div>

      <div>
            <table>
                  <thead>
                        <tr>
                              <th>Name</th>
                              <th>ISIN</th>
                              <th>Aktueller Wert</th>
                              <th>Gewinn/Verlust</th>
                        </tr>
                  </thead>
                  <tbody>
                        <tr v-for="position in positions" :key="position.id" @click="navigateToStockDetail(position.symbol)">
                              <td>{{ position.name }}</td>
                              <td>{{ position.symbol }}</td>
                              <td>{{ position.currentValue }} €</td>
                              <td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
                                    {{ position.change }} € ({{ position.changePercentage }}%)
                              </td>
                        </tr>
                  </tbody>
            </table>
      </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import {useRouter} from "vue-router";

const router = useRouter()
const searchField = ref('')
const positions = ref([
  { id: 1, name: 'Apple', symbol: "2", currentValue: 1450.90, change: 33.39, changePercentage: 13.6 },
  { id: 2, name: 'Tesla', symbol: "5", currentValue: 3565.35, change: 120.75, changePercentage: 20.1 },
  { id: 3, name: 'Amazon', symbol: "10", currentValue: 6169.52, change: -45.50, changePercentage: -5.2 },
])

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
