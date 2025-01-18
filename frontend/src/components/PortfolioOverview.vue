<template>
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>Investment-Depot</h1>
        <p v-if="investmentAccount && investmentAccount.owner">{{ investmentAccount.owner.name }}</p>
        <p v-if="investmentAccount && investmentAccount.owner">{{ investmentAccount.owner.mail }}</p>
      </div>
      <div class="total-value" v-if="investmentAccount && investmentAccount.owner">
        <p>Depotwert: <strong>{{ totalValue }} €</strong></p>
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
        <td :class="{ 'positive': portfolioEntry.stock.change >= 0, 'negative': portfolioEntry.stock.change < 0 }">
          {{ portfolioEntry.totalValue }} € ({{ portfolioEntry.changePercentage }}%)
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {useRoute, useRouter} from "vue-router";
import type {InvestmentAccount} from "@/types/types.ts";
import {onBeforeMount, onUnmounted, ref} from 'vue';

const router = useRouter()
const route = useRoute()
const investmentAccount = ref<InvestmentAccount>()
const totalValue = ref<number>()
let pollingIntervalID: number
const investmentAccountId = route.params.investmentAccountId as string

onBeforeMount(async () => {
  try {
    const response = await fetch(`/api/portfolio?investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const result = await response.json() as InvestmentAccount

    investmentAccount.value = result
    totalValue.value = result.totalValue
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)
})

async function poll() {
  console.log("polling");

  try {
    const response = await fetch(`/api/portfolio?investmentAccountId=${investmentAccountId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const investmentAccountData = (await response.json()) as InvestmentAccount;
    totalValue.value = investmentAccountData.totalValue

    for (const portfolioEntry of investmentAccount.value!.portfolio) {
      const matchingEntry = investmentAccountData.portfolio.find(
        (entry) => portfolioEntry.id === entry.id
      );

      if (matchingEntry && portfolioEntry.stock.latestQuote.currentPrice !== matchingEntry.stock.latestQuote.currentPrice) {
        portfolioEntry.stock.latestQuote.currentPrice = matchingEntry.stock.latestQuote.currentPrice;
        portfolioEntry.stock.justChanged = true;

        setTimeout(() => {
          portfolioEntry.stock.justChanged = false;
        }, 200);
      }
    }
  } catch (e) {
    console.error(e);
  }
}

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})


const navigateToStockDetail = (symbol: string, investmentAccountId: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol, investmentAccountId}});
}


</script>

<style lang="scss">
@use "./style.scss";
</style>


<style lang="scss">
@use "./style.scss";

.just-changed {
  background-color: var(--main-color-light);
}

.table-row {
  transition: background-color 200ms;
}
</style>
