<template>
  <div class="invest-depot">
    <div>
      <h1>Edelmetalle</h1>
      <div><!--Leeres Element für Grid--></div>
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
        <tr class="table-row" v-for="metal in filteredMetals" :key="metal.symbol" :class="{ 'just-changed': metal.justChanged}" @click="navigateToMetalDetail(metal.symbol)">
          <td>{{ metal.name }}</td>
          <td>{{ metal.symbol }}</td>
          <td>USD</td>
          <td>{{ metal.currentPrice }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, onUnmounted, ref, computed} from "vue"
import {type Metal} from "@/types/types.ts"
import {useRouter} from "vue-router"

const router = useRouter()
let pollingIntervalID: number

const priceDescending = ref<boolean>(false)
const nameDescending = ref<boolean>(false)

const search = ref<string>('')
const metals = ref<Metal[]>([])
const  filteredMetals = computed(() =>
  metals.value.filter(metal => {
    return metal.symbol.toLowerCase().includes(search.value.toLowerCase()) || metal.name.toLowerCase().includes(search.value.toLowerCase())
  })
)

function resetSearch() {
  search.value = ''
}

async function poll() {

  console.log("polling")

  for (const metal of metals.value) {
    try {
      const response = await fetch(`/api/metal?symbol=${metal.symbol}`)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const metalData = await response.json() as Metal
      if (metal.currentPrice !== metalData.currentPrice) {
        metal.currentPrice = metalData.currentPrice
        metal.justChanged = true

        setTimeout(() => {
          metal.justChanged = false
        }, 200)
      }
    } catch (e) {
      console.error(e)
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
    metals.value = [...metals.value].sort((a, b) => a.currentPrice - b.currentPrice)
  } else {
    metals.value = [...metals.value].sort((a, b) => b.currentPrice - a.currentPrice)
  }

}

function sortByName() {
  nameDescending.value = !nameDescending.value
  if(priceDescending.value == true) {
    priceDescending.value = false
  }
  if(nameDescending.value) {
    metals.value = [...metals.value].sort((a, b) => a.name.localeCompare(b.name))
  } else {
    metals.value = [...metals.value].sort((a, b) => b.name.localeCompare(a.name))
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

  sortByName()
  pollingIntervalID = setInterval(poll, 3000)

})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const navigateToMetalDetail = (symbol: string) => {
  console.log("Trying to navigate")
  router.push({name: 'metall-detail', params: {symbol}})
}

</script>

<style lang="scss">
@use "./style.scss";
</style>
