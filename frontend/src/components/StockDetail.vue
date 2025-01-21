<template>
  <div class="stock-detail">
    <div class="stock-detail-header">
      <div class="stock-detail-header-info">
        <h2>{{ stockDetails.stock.name }} - Detailansicht</h2>
        <p><strong>Symbol:</strong> {{ stockDetails.stock.symbol }}</p>
        <p><strong>FIGI:</strong> {{ stockDetails.stock.figi }}</p>
        <p :class="{ 'just-changed': stockDetails.stock.justChanged}"><strong>Aktueller Wert:</strong>
          {{ stockDetails.stock.latestQuote.currentPrice }} {{ stockDetails.stock.currency }}</p>
        <p><strong>Beschreibung:</strong> {{ stockDetails.stock.description }}</p>

        <div class="purchase-buttons">
          <button class="purchase-button" @click="purchase(stockDetails.stock.symbol, stockDetails.portfolioEntry.investmentAccountId)">
            Kaufen</button>
          <button class="purchase-button" @click="sell(stockDetails.stock.symbol, stockDetails.portfolioEntry.investmentAccountId)">Verkaufen</button>
        </div>
      </div>
      <div class="stock-detail-header-chart">
        <Line ref="lineChart" :data="data" :options="options"/>
      </div>
    </div>

    <div class="stock-info-section" v-if="stockDetails.portfolioEntry != null">
      <h2>Im Besitz</h2>
      <table>
        <tbody>
        <tr>
          <td>Stück</td>
          <td>{{ stockDetails.portfolioEntry.quantity }}</td>
        </tr>
        <tr>
          <td>Gesamtwert</td>
          <td>{{ stockDetails.portfolioEntry.totalValue}} {{ stockDetails.stock.currency }}</td>
        </tr>
        <tr>
          <td>Seit Kauf</td>
          <td :class="{ 'positive': stockDetails.portfolioEntry.profitAndLossPercent >= 0, 'negative':
          stockDetails.portfolioEntry.profitAndLossPercent < 0 }">
            {{ stockDetails.portfolioEntry.profitAndLoss }} {{ stockDetails.stock.currency }} ({{ stockDetails.portfolioEntry.profitAndLossPercent }}%)
          </td>
        </tr>
        <tr>
          <td>Kaufpreis gesamt</td>
          <td>{{ stockDetails.portfolioEntry.totalInvestAmount }} {{ stockDetails.stock.currency }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="stock-info-section">
      <h2>Kurse</h2>
      <table>
        <tbody>
        <tr>
          <td>Tagestief</td>
          <td>{{ stockDetails.stock.latestQuote.lowPriceOfTheDay }} {{stockDetails.stock.currency}}</td>
        </tr>
        <tr>
          <td>Tageshoch</td>
          <td>{{ stockDetails.stock.latestQuote.highPriceOfTheDay }} {{stockDetails.stock.currency}}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onBeforeMount, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type {StockDetails} from '@/types/types.ts'
import {CategoryScale, Chart as ChartJS, Legend, LinearScale, LineElement, PointElement, Title, Tooltip} from 'chart.js'
import {Line} from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

interface DataPoint {
  content: number;
}

const numDataPoints = 60
const dataPoints = []
const labels = []

const data = {
  labels: labels,
  datasets: [
    {
      label: '',
      backgroundColor: '#f87979',
      data: dataPoints
    }
  ]
}

const options = {
  maintainAspectRatio: false,
  scales: {
    x: {
      display: false // Keine Labels auf der x-Achse
    }
  },
  plugins: {
    legend: {
      display: false, // Deaktiviert die Legende (farbiger Button)
    },
    tooltip: {
      enabled: false, // Deaktiviert die Tooltips
    }
  }
}

const lineChart = ref(null)
const stockDetails = ref<StockDetails>({})
let pollingIntervalID: number

async function poll() {
  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  try {
    const response = await
      fetch(`/api/stock-details/symbol?symbol=${stockSymbol}&investmentAccountId=${investmentAccountId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const stockData = await response.json() as StockDetails;
    if (stockDetails.value.stock.latestQuote.currentPrice !== stockData.stock.latestQuote.currentPrice) {
      stockDetails.value.stock.latestQuote.currentPrice = stockData.stock.latestQuote.currentPrice
      stockDetails.value.portfolioEntry.totalValue = stockData.portfolioEntry.totalValue
      stockDetails.value.portfolioEntry.profitAndLoss = stockData.portfolioEntry.profitAndLoss
      stockDetails.value.portfolioEntry.profitAndLossPercent = stockData.portfolioEntry.profitAndLossPercent
      stockDetails.value.stock.latestQuote.highPriceOfTheDay = stockData.stock.latestQuote.highPriceOfTheDay
      stockDetails.value.stock.latestQuote.lowPriceOfTheDay = stockData.stock.latestQuote.lowPriceOfTheDay

      stockDetails.value.stock.justChanged = true

      setTimeout(() => {
        stockDetails.value.stock.justChanged = false;
      }, 200);
    }
    if(dataPoints.length >= numDataPoints) {
      dataPoints.shift();
      labels.shift();
    }
    dataPoints.push(stockData.stock.latestQuote.currentPrice);
    labels.push('0')
    if (lineChart.value) {
      lineChart.value.chart.update();
    } else {
      console.log("Instanz ist null")
    }
  } catch (e) {
    console.error(e);
  }
}

onBeforeMount(async () => {
  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  try {
    const response = await
      fetch(`/api/stock-details/symbol?symbol=${stockSymbol}&investmentAccountId=${investmentAccountId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    stockDetails.value = await response.json() as StockDetails
    console.log(stockDetails.value)

  } catch (e) {
    console.error(e);
  }

  if (pollingIntervalID) {
    clearInterval(pollingIntervalID);
  }
  pollingIntervalID = setInterval(poll, 3000)
})

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const router = useRouter()

function sell(symbol: string, investmentAccountId: number) {
  console.log('Verkaufen')
  router.push({name: 'order-management-sell', params: {symbol, investmentAccountId}});
}

function purchase(symbol: string, investmentAccountId: number) {
  console.log('Kaufen')
  router.push({name: 'order-management-buy', params: {symbol, investmentAccountId}});
}

</script>

<style lang="scss">
@use "./style.scss";

.stock-detail-header {
  display: flex;
  align-content: center;
  justify-content: space-between;
}

.stock-detail-header-info {
  width: 40%;
}

.stock-detail-header-chart {
  width: 50%;
  padding: 5% 0 5% 5%;
}
</style>

