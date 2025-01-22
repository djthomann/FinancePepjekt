<template>
  <div class="invest-depot">
    <div>
      <h1>Kryptowährungen</h1>
      <div id="searchField">
        <input v-model="search" placeholder="Symbol/Name" />
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
        </tr>
        </thead>
        <tbody>
        <tr class="table-row" v-for="coin in filteredCoins" :key="coin.symbol" :class="{ 'just-changed': coin.justChanged}" @click="navigateToCryptoDetail(coin.symbol)">
          <td>{{ coin.name }}</td>
          <td>{{ coin.symbol }}</td>
          <td>USD</td>
          <td>{{ coin.currentPrice }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref} from "vue";
import {Coin} from "@/types/types.ts";
import {useRouter} from "vue-router";

const router = useRouter()
let pollingIntervalID: number

const priceDescending = ref<boolean>(false)
const nameDescending = ref<boolean>(false)

const search = ref<string>('')
const coins = ref<Coin[]>([])
const filteredCoins = computed(() =>
  coins.value.filter(coin => {
    return coin.symbol.toLowerCase().includes(search.value.toLowerCase()) || coin.name.toLowerCase().includes(search.value.toLowerCase())
  })
);

function resetSearch() {
  search.value = ''
}

async function poll() {

  console.log("polling")

  for (const coin of coins.value) {
    try {
      const response = await fetch(`/api/crypto?symbol=${coin.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const cryptoData = await response.json() as Coin;
      if(coin.currentPrice !== cryptoData.currentPrice) {
        coin.currentPrice = cryptoData.currentPrice
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
  priceDescending.value = !priceDescending.value
  if(nameDescending.value == true) {
    nameDescending.value = false
  }
  console.log(priceDescending.value)
  if(priceDescending.value) {
    coins.value = [...coins.value].sort((a, b) => a.currentPrice - b.currentPrice)
  } else {
    coins.value = [...coins.value].sort((a, b) => b.currentPrice - a.currentPrice)
  }

}

function sortByName() {
  nameDescending.value = !nameDescending.value
  if(priceDescending.value == true) {
    priceDescending.value = false
  }
  if(nameDescending.value) {
    coins.value = [...coins.value].sort((a, b) => a.name.localeCompare(b.name));
  } else {
    coins.value = [...coins.value].sort((a, b) => b.name.localeCompare(a.name));
  }
}

onMounted(async () => {

  try {
    const response = await fetch(`/api/crypto`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    coins.value = await response.json() as Crypto[]
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

.sorting-button {
  display: flex;
  align-items: center;
  background: 0;
  border: none;
  font-weight: bold;
  font-size: 15px;
  color: #333;
}

.sorting-button:hover {
  cursor: pointer;
}

.sorting-button::before {
  margin-right: 10px;
  width: 10px;
  height: 10px;
  content: '';
  background: url('@/assets/arrow_down.svg') no-repeat center center;
  background-size: contain;
  transition: transform 200ms;
  transform: rotate(180deg);
}
.sorting-button-down::before {
  transform: rotate(0deg);
}

</style>
