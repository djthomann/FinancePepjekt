<template>
  <div class="invest-depot">
    <div>
      <h1>Edelmetalle</h1>
      <div id="searchField">
        <input  placeholder="Symbol/Name" />
        <button class="details-button" @click="resetSearch">Reset</button>
        <button class="details-button" @click="searchContent">Search</button>
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
          <th>Gewinn/Verlust</th>
        </tr>
        </thead>
        <tbody>
        <tr class="table-row" v-for="metal in metals" :key="metal.symbol" :class="{ 'just-changed': metal.justChanged}" @click="navigateToMetalDetail(metal.symbol)">
          <td>{{ metal.name }}</td>
          <td>{{ metal.symbol }}</td>
          <td>USD</td>
          <td>{{ metal.cprice }}</td>
          <td :class="{ 'positive': metal.change >= 0, 'negative': metal.change < 0 }">
            {{ metal.change }} € ({{ metal.changePercentage }}%)
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, onUnmounted, ref} from "vue";
import {type Metal} from "@/types/types.ts";
import {useRouter} from "vue-router";

const router = useRouter()
let pollingIntervalID: number

const priceDescending = ref<boolean>(false)
const nameDescending = ref<boolean>(false)

const metals = ref<Metal[]>([])

async function poll() {

  console.log("polling")

  for (const metal of metals.value) {
    try {
      const response = await fetch(`/api/metal?symbol=${metal.symbol}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const metalData = await response.json() as Metal;
      console.log(metalData)
      if (metal.cprice !== metalData.cprice) {
        metal.cprice = metalData.cprice
        metal.justChanged = true

        setTimeout(() => {
          metal.justChanged = false;
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
    metals.value = [...metals.value].sort((a, b) => a.cprice - b.cprice)
  } else {
    metals.value = [...metals.value].sort((a, b) => b.cprice - a.cprice)
  }

}

function sortByName() {
  nameDescending.value = !nameDescending.value
  if(priceDescending.value == true) {
    priceDescending.value = false
  }
  if(nameDescending.value) {
    metals.value = [...metals.value].sort((a, b) => a.name.localeCompare(b.name));
  } else {
    metals.value = [...metals.value].sort((a, b) => b.name.localeCompare(a.name));
  }
}

onMounted(async () => {

  try {
    const response = await fetch("/api/metal")
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    metals.value = await response.json() as Metal[]
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)

})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const navigateToMetalDetail = (symbol: string) => {
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
