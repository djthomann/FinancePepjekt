<template>
  <div class="invest-depot">
    <div>
      <h1>Edelmetalle</h1>
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
          <th ><button @click="sortByName">Name</button></th>
          <th>Symbol</th>
          <th>Währung</th>
          <th ><button @click="sortByPrice">Aktueller Wert</button></th>
          <th>Gewinn/Verlust</th>
        </tr>
        </thead>
        <tbody>
        <tr class="table-row" v-for="coin in coins" :key="coin.symbol" :class="{ 'just-changed': coin.justChanged}" @click="navigateToMetalDetail(coin.symbol)">
          <td>{{ coin.name }}</td>
          <td>{{ coin.symbol }}</td>
          <td>USD</td>
          <td>{{ coin.cprice }}</td>
          <td :class="{ 'positive': coin.change >= 0, 'negative': coin.change < 0 }">
            {{ coin.change }} € ({{ coin.changePercentage }}%)
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, onUnmounted} from "vue";
import {} from "@/types/types.ts";
import {useRouter} from "vue-router";

const router = useRouter()
let pollingIntervalID: number

async function poll() {

  console.log("polling")


}

onMounted(async () => {


})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const navigateToMetalDetail = (symbol: string) => {
}

</script>

<style scoped lang="scss">
.just-changed {
  background-color: var(--main-color-light);
}

.table-row {
  transition: background-color 200ms;
}
</style>
