<template>
  <div class="stock-order">
    <h2>{{ stock.name }} - Kaufansicht</h2>
    <div id="info">

      <div>
        <p><strong>FIGI:</strong> {{ stock.figi }}</p>
        <p><strong>Aktueller Wert:</strong> {{ stock.latestQuote.currentPrice }} €</p>

      </div>
      <div class="info-box bankaccount-info">
        <h3>Bankguthaben:</h3>
        <p><strong>{{ bankAccountBalance }} USD</strong></p>
      </div>

    </div>
    <div>
      <form @submit.prevent="purchase">
        <!--  <label for="amount">Anzahl</label>
        <input v-model.number="amount" type="number" id="amount" />
         oder
         -->
        <label for="purchaseAmount">Kaufsumme</label>
        <input v-model.number="purchaseAmount" type="number" id="sum" step="0.01" />
        <br>
        <label for="timestamp-date">Datum</label>
        <input v-model="date" type="date" id="timestamp-date"/>
        <label for="timestamp-time">Uhrzeit</label>
        <input v-model="time" type="time" id="timestamp-time"/>
        <br>
        <button type="submit" class="purchase-button">Kaufen</button>
        <p>{{ serverResponse }}</p>
      </form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, ref} from 'vue'
import {useRoute} from 'vue-router'
import type {InvestmentAccount, Stock} from '@/types/types.ts'

// Daten und Referenzen
const stock = ref<Stock>({})
const purchaseAmount = ref<number>()
const serverResponse = ref<string>()

const date = ref(getTodayDate())
const time = ref("00:00")

const route = useRoute()
const investmentAccountId = route.params.investmentAccountId
const investmentAccount = ref<InvestmentAccount>()
const bankAccountId = ref<number>()
const bankAccountBalance = ref<number>()

onBeforeMount(async () => {
  const symbol = route.params.symbol

  try {
    const response = await fetch(`/api/stock/by/symbol?symbol=${symbol}&investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stock.value = await response.json() as Stock
  } catch (e) {
    console.error(e)
  }

  // Get corresponding bankAccountId for investment account
  try {
    const response = await fetch(`/api/bankaccount?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    bankAccountId.value = await response.json() as number
  } catch (e) {
    console.error(e)
  }

  // Get bankAccount balance
  await fetchBankAccountBalance()
})

async function purchase() {
  const route = useRoute()
  const stockSymbol = Array.isArray(route.params.symbol) ? route.params.symbol[0] : route.params.symbol
  const investmentAccountId = Array.isArray(route.params.investmentAccountId)
    ? route.params.investmentAccountId[0]
    : route.params.investmentAccountId

  const executionTime = `${date.value}T${time.value}`

  const baseUrl = "/api/placeBuyOrder"
  const queryParams = new URLSearchParams({
    investmentAccountId: investmentAccountId,
    stockSymbol: stockSymbol,
    purchaseAmount: purchaseAmount.value!.toString(),
    executionTime,
  }).toString()

  const requestUrl = `${baseUrl}?${queryParams}`

  try {
    const response = await fetch(requestUrl, {method: "POST"})

    if (!response.ok) {
      serverResponse.value = "Error: Order invalid"
      throw new Error("Network response was not ok")
    }

    serverResponse.value = "Success: Buy order placed"
  } catch (error) {
    serverResponse.value = "Server Error: Order not placed"
    console.error("Error: Order not placed", error)
  }
}

function getTodayDate() {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function fetchBankAccountBalance() {
  try {
    const response = await fetch(`/api/bankaccount/balance?bankAccountId=${bankAccountId.value}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const result = await response.json() as number
    bankAccountBalance.value = result
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss">
@use "./style.scss";

#info {
  display: flex;
  justify-content: space-between;
}
</style>
