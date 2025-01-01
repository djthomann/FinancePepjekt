<template>
      <div>
            <h1>Wertpapiere</h1>
            <div id="searchField">
                  <input v-model="searchField" placeholder="ISIN/Name" />
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
                        <tr v-for="position in positions" :key="position.id">
                              <td>{{ position.name }}</td>
                              <td>{{ position.isin }}</td>
                              <td>{{ position.currentValue }} €</td>
                              <td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
                                    {{ position.change }} € ({{ position.changePercentage }}%)
                              </td>
                        </tr>
                  </tbody>
            </table>
      </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

const searchField = ref('')
const positions = ref([
  { id: 1, name: 'Apple', isin: 'US0378331005', currentValue: 1450.90, change: 33.39, changePercentage: 13.6 },
  { id: 2, name: 'Tesla', isin: 'US88160R1014', currentValue: 3565.35, change: 120.75, changePercentage: 20.1 },
  { id: 3, name: 'Amazon', isin: 'US0231351067', currentValue: 6169.52, change: -45.50, changePercentage: -5.2 },
])

function resetSearch() {
  searchField.value = ''
}

function searchContent() {
  console.log('searching for:', searchField.value)
}

</script>