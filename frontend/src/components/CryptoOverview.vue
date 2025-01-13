<template>
  <div class="invest-depot">
    <div>
      <h1>Kryptowährungen</h1>
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
        <tr class="table-row" v-for="coin in coins" :key="coin.symbol" :class="{ 'just-changed': coin.justChanged}" @click="navigateToCryptoDetail(coin.symbol)">
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
import {onMounted, onUnmounted, ref, computed} from "vue";
import {Coin, type Stock} from "@/types/types.ts";
import {useRouter} from "vue-router";

const router = useRouter()
let pollingIntervalID: number

let coins = ref<Coin[]>([])

async function poll() {

  console.log("polling")

  for (const coin of coins.value) {
    try {
      const response = await fetch(`/api/crypto?symbol=${coin.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const cryptoData = await response.json() as Coin;
      if(coin.cprice !== cryptoData.cprice) {
        coin.cprice = cryptoData.cprice
        coin.justChanged = true

        setTimeout(() => {
          coin.justChanged = false;
        }, 200);
      }
    } catch (e) {
      console.error(e);
    }

  }
}

function sortByPrice() {
  coins.value = [...coins.value].sort((a, b) => b.cprice - a.cprice)
}

function sortByName() {
  coins.value = [...coins.value].sort((a, b) => a.symbol.localeCompare(b.symbol)); // Absteigend
}

onMounted(async () => {

  try {
    const response = await fetch(`/api/crypto`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    let responseJSON = await response.json()
    console.log(responseJSON)
    coins.value = responseJSON as Crypto[]
  } catch (e) {
    console.error(e)
  }
  sortByName()
  pollingIntervalID = setInterval(poll, 1000)
})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const navigateToCryptoDetail = (symbol: string) => {
  console.log("Trying to navigate")
  router.push({name: 'krypto-detail', params: {symbol}});
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
