<template>
  <div class="invest-depot">
    <div class="header">
      <div class="title">
        <h1>Investment-Depot</h1>
        <p>{{ investmentAccount.user.name }}</p>
        <p>{{ investmentAccount.user.mail}}</p>
      </div>
      <div class="total-value">
        <p>Depotwert: <strong>{{ totalValue }} €</strong></p>
      </div>
    </div>

    <h2>Portfolio-Wertpapiere</h2>
    <table>
      <thead>
      <tr>
        <th>Name</th>
        <th>Symbol</th>
        <th>Aktueller Wert</th>
        <th>Anteile</th>
        <th>Gewinn/Verlust</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="portfolioEntry in investmentAccount.portfolio" :key="portfolioEntry.id" @click="navigateToStockDetail(portfolioEntry.stockSymbol)">
        <td>{{ portfolioEntry.stock.description }}</td>
        <td>{{ portfolioEntry.stockSymbol }}</td>
        <td>{{ portfolioEntry.currentValue }} €</td>    <!--reactive value-->
        <td>{{ portfolioEntry.amount }}</td>    <!--reactive value-->
        <td :class="{ 'positive': portfolioEntry.change >= 0, 'negative': portfolioEntry.change < 0 }">
          {{ portfolioEntry.change }} € ({{ portfolioEntry.changePercentage }}%)
        </td>         <!--reactive value-->
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {InvestmentAccount} from "@/types/types.ts";

const router = useRouter()
const route = useRoute()
const investmentAccountId = route.params.investmentAccountId
const totalValue = ref(0.0)
const investmentAccount = ref<InvestmentAccount>({})


onMounted(async () => {
  try {
    const responsePortfolio = await fetch(`/api/portfolio?investmentAccountId=${investmentAccountId}`)
    if (!responsePortfolio.ok) {
      throw new Error(`HTTP error! status: ${responsePortfolio.status}`)
    }
    investmentAccount.value = await responsePortfolio.json() as InvestmentAccount

    const responseTotalValue = await fetch(`/api/portfolio/totalValue?investmentAccountId=${investmentAccountId}`)
    if (!responseTotalValue.ok) {
      throw new Error(`HTTP error! status: ${responseTotalValue.status}`)
    }
    totalValue.value = await responseTotalValue.json() as number
  } catch (e) {
    console.error(e)
  }
})

const navigateToStockDetail = (symbol: string) => {
  router.push({name: 'wertpapier-detail', params: {symbol}});
}

</script>

<style lang="scss">
@use "./style.scss";
</style>


<style lang="scss">
@use "./style.scss";
</style>
