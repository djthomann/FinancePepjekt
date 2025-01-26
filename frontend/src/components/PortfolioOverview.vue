<template>
  <div class="invest-depot">
    <h1>Investment-Depot</h1>
    <div class="header">
      <div class="info-box">
        <h3>Kundeninformationen:</h3>
        <p v-if="investmentAccount && investmentAccount.owner"><strong>{{ investmentAccount.owner.name }}</strong></p>
        <p v-if="investmentAccount && investmentAccount.owner">{{ investmentAccount.owner.mail }}</p>
      </div>
      <div class="info-box bankaccount-info">
        <h3>Bankguthaben:</h3>
        <p><strong>{{ bankAccountBalance }} USD</strong></p>
        <input @keyup.enter="deposit" v-model="depositAmount" name="increase-balance" type="number" placeholder="Aufladen">
        <p>{{depositServerAnswer}}</p>
      </div>
      <div class="info-box total-value" v-if="investmentAccount && investmentAccount.owner">
        <h3>Depotwert:</h3>
        <p><strong>{{ totalValue }} USD</strong></p>
        <p :class="{ 'positive':totalProfitAndLossPercent >= 0, 'negative': totalProfitAndLossPercent < 0 }">
          {{ totalProfitAndLoss }} ({{ totalProfitAndLossPercent }}%)
        </p>
      </div>
    </div>

    <h2>Portfolio-Wertpapiere</h2>
    <table v-if="investmentAccount && investmentAccount.portfolio">
      <thead>
      <tr>
        <th>Name</th>
        <th>Symbol</th>
        <th>Anteile</th>
        <th>Aktueller Wert</th>
        <th>Gesamtwert</th>
        <th>Bilanz</th>
      </tr>
      </thead>
      <tbody>

      <tr class="table-row" :class="{ 'just-changed': portfolioEntry.stock.justChanged}"
          v-for="portfolioEntry in investmentAccount.portfolio" :key="portfolioEntry.stock.symbol"
          @click="navigateToStockDetail(portfolioEntry.stock.symbol, investmentAccountId)">
        <td>{{ portfolioEntry.stock.name }}</td>
        <td>{{ portfolioEntry.stock.symbol }}</td>
        <td>{{ portfolioEntry.quantity }}</td>
        <td>{{ portfolioEntry.stock.latestQuote.currentPrice }}</td>
        <td>{{ portfolioEntry.totalValue }}</td>
        <td
          :class="{ 'positive': portfolioEntry.profitAndLossPercent >= 0, 'negative': portfolioEntry.profitAndLossPercent < 0 }">
          {{ portfolioEntry.profitAndLoss }} ({{ portfolioEntry.profitAndLossPercent }}%)
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {useRoute, useRouter} from "vue-router"
import type {InvestmentAccount} from "@/types/types.ts"
import {onBeforeMount, onUnmounted, ref} from 'vue'

const router = useRouter()
const route = useRoute()
const investmentAccount = ref<InvestmentAccount>()
const totalValue = ref<number>()
const totalProfitAndLoss = ref<number>(0)
const totalProfitAndLossPercent = ref<number>(0)
let pollingIntervalID: number
const investmentAccountId = route.params.investmentAccountId as string
const bankAccountId = ref<number>()
const bankAccountBalance = ref<number>()
const depositAmount = ref<number>()
const depositServerAnswer = ref<string>('')

onBeforeMount(async () => {
  try {
    const response = await fetch(`/api/portfolio?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const result = await response.json() as InvestmentAccount

    investmentAccount.value = result

    totalProfitAndLoss.value = result.totalProfitAndLoss
    totalProfitAndLossPercent.value = result.totalProfitAndLossPercent
    totalValue.value = result.totalValue
  } catch (e) {
    console.error(e)
  }

  // Get corresponding bankAccountId for investment account
  try {
    const response = await fetch(`/api/bankaccount?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const result = await response.json() as number
    bankAccountId.value = result
  } catch (e) {
    console.error(e)
  }

  // Get bankAccount balance
  await fetchBankAccountBalance()

  pollingIntervalID = setInterval(poll, 3000)
})

async function poll() {
  console.log("polling")

  try {
    const response = await fetch(`/api/portfolio?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const investmentAccountData = (await response.json()) as InvestmentAccount
    totalValue.value = investmentAccountData.totalValue
    totalProfitAndLoss.value = investmentAccountData.totalProfitAndLoss
    totalProfitAndLossPercent.value = investmentAccountData.totalProfitAndLossPercent

    for (const portfolioEntry of investmentAccount.value!.portfolio) {
      const matchingEntry = investmentAccountData.portfolio.find(
        (entry) => portfolioEntry.id === entry.id
      )

      if (matchingEntry && portfolioEntry.stock.latestQuote.currentPrice !== matchingEntry.stock.latestQuote.currentPrice) {
        portfolioEntry.stock.latestQuote.currentPrice = matchingEntry.stock.latestQuote.currentPrice
        portfolioEntry.totalValue = matchingEntry.totalValue
        portfolioEntry.quantity = matchingEntry.quantity
        portfolioEntry.profitAndLoss = matchingEntry.profitAndLoss
        portfolioEntry.profitAndLossPercent = matchingEntry.profitAndLossPercent
        portfolioEntry.stock.justChanged = true

        setTimeout(() => {
          portfolioEntry.stock.justChanged = false
        }, 200)
      }
    }
  } catch (e) {
    console.error(e)
  }

  await fetchBankAccountBalance()
}

async function deposit() {

  console.log("Depositing: " + depositAmount.value)

  try {
    const url = `/api/bankaccount/deposit?bankAccountId=${bankAccountId.value}&amount=${depositAmount.value}`
    const response = await fetch(url, {method: "POST"})

    if (!response.ok) {
      serverResponse.value = "Error: Order invalid"
      throw new Error("Network response was not ok")
    }

    depositServerAnswer.value = "Success: Deposit done"
  } catch (error) {
    depositServerAnswer.value = "Error: Deposit failed"
    console.error("Error: Order not placed", error)
  }

  await fetchBankAccountBalance()
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

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})


const navigateToStockDetail = (symbol: string, investmentAccountId: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol, investmentAccountId}})
}


</script>

<style lang="scss">
@use "./style.scss";

.just-changed {
  background-color: var(--main-color-light);
}

.table-row {
  transition: background-color 200ms;
}


</style>
