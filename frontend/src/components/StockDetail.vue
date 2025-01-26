<template>
  <div class="stock-detail">
    <div class="stock-detail-header">
      <div class="stock-detail-header-info">
        <h2>{{ stockDetails.stock.name }} - Detailansicht</h2>
        <p><strong>Symbol:</strong> {{ stockDetails.stock.symbol }}</p>
        <p><strong>FIGI:</strong> {{ stockDetails.stock.figi }}</p>
        <p :class="{ 'just-changed': stockDetails.stock.justChanged}"><strong>Aktueller Wert:</strong>
          {{ stockDetails.stock.latestQuote.currentPrice }} {{ stockDetails.stock.currency }}</p>
        <p><strong>Durchschnittlicher Wert:</strong>
          {{ averagePrice }}</p>
        <p><strong>Beschreibung:</strong> {{ stockDetails.stock.description }}</p>

        <div class="purchase-buttons">
          <button class="purchase-button" @click="purchase(stockDetails.stock.symbol)">
            Kaufen</button>
          <button v-if="stockDetails.portfolioEntry != null" class="purchase-button"
                  @click="sell(stockDetails.stock.symbol)">Verkaufen
          </button>
        </div>
      </div>
      <div class="stock-detail-header-chart-box">
        <div class="stock-detail-header-chart">
          <Line ref="lineChart" :data="data" :options="options"/>
        </div>
        <div class="stock-detail-header-chart-buttons">
          <button @click="fetchLastMinutes(1)">
            1 min
          </button>
          <button @click="fetchLastMinutes(5)">
            5 min
          </button>
          <button @click="fetchLastMinutes(10)">
            10 min
          </button>
        </div>
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
import {onBeforeMount, onUnmounted, ref} from 'vue'
import {useRoute, useRouter} from "vue-router"
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

let numDataPoints = 60
let dataPoints: number[] = []
let averagePriceData = []
let labels = []
const lineChart = ref(null)
const stockDetails = ref<StockDetails>({})
const averagePrice = ref<number>(0)
let pollingIntervalID: number

const data = {
  labels: labels,
  datasets: [
    {
      label: 'data',
      backgroundColor: 'transparent',
      borderColor: 'grey',
      borderWidth: 1,
      pointBorderWidth: 0,
      stepped: false,
      data: dataPoints
    },
    {
      label: 'average',
      backgroundColor: 'red',
      borderColor: 'grey',
      borderWidth: 1,
      pointBorderWidth: 0,
      stepped: false,
      data: averagePriceData
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
      display: true, // Deaktiviert die Legende (farbiger Button)
    },
    tooltip: {
      enabled: false, // Deaktiviert die Tooltips
    }
  }
}

async function poll() {

  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  const minutes = dataPoints.length / 60
  await fetchAveragePrice(minutes, 0, 0, 0)

  try {
    const response = await
      fetch(`/api/stock-details/symbol?symbol=${stockSymbol}&investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const stockData = await response.json() as StockDetails
    if (stockDetails.value.stock.latestQuote.currentPrice !== stockData.stock.latestQuote.currentPrice) {
      stockDetails.value.stock.latestQuote.currentPrice = stockData.stock.latestQuote.currentPrice
      stockDetails.value.portfolioEntry.totalValue = stockData.portfolioEntry.totalValue
      stockDetails.value.portfolioEntry.profitAndLoss = stockData.portfolioEntry.profitAndLoss
      stockDetails.value.portfolioEntry.profitAndLossPercent = stockData.portfolioEntry.profitAndLossPercent
      stockDetails.value.stock.latestQuote.highPriceOfTheDay = stockData.stock.latestQuote.highPriceOfTheDay
      stockDetails.value.stock.latestQuote.lowPriceOfTheDay = stockData.stock.latestQuote.lowPriceOfTheDay

      stockDetails.value.stock.justChanged = true

      setTimeout(() => {
        stockDetails.value.stock.justChanged = false
      }, 200)
    }
    if(dataPoints.length >= numDataPoints) {
      dataPoints.shift()
      labels.shift()
    }
    dataPoints.push(stockData.stock.latestQuote.currentPrice)
    labels.push('0')
    if (lineChart.value) {
      lineChart.value.chart.update()
    } else {
      console.log("Instanz ist null")
    }
  } catch (e) {
    console.error(e)
  }
}

onBeforeMount(async () => {
  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  try {
    const response = await
      fetch(`/api/stock-details/symbol?symbol=${stockSymbol}&investmentAccountId=${investmentAccountId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stockDetails.value = await response.json() as StockDetails

  } catch (e) {
    console.error(e)
  }

  await fetchLastMinutes(1)
  const minutes = dataPoints.length / 60
  await fetchAveragePrice(minutes, 0, 0, 0)

  if (pollingIntervalID) {
    clearInterval(pollingIntervalID);
  }
  pollingIntervalID = setInterval(poll, 1000)
})

onUnmounted(() => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const router = useRouter()

function sell(symbol: string) {
  const route = useRoute()
  const investmentAccountId = route.params.investmentAccountId

  router.push({name: 'order-management-sell', params: {symbol, investmentAccountId}})
}

function purchase(symbol: string) {
  const route = useRoute()
  const investmentAccountId = route.params.investmentAccountId
  router.push({name: 'order-management-buy', params: {symbol, investmentAccountId}})
}

async function fetchLastMinutes(min: number) {
  const route = useRoute()
  const stockSymbol = route.params.symbol

  try {
    const response = await fetch(`/api/stock/history/symbol?symbol=${stockSymbol}&from=${getDateTimeByOffset(min, 0)}&to=${getDateTimeByOffset(0, 0)}` )
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const history = await response.json() as Quote[]
    numDataPoints = history.length
    const prices = history.map(quote => quote.currentPrice)

    for(let price of prices) {
      while(dataPoints.length >= numDataPoints) {
        dataPoints.shift()
        labels.shift()
      }
      dataPoints.push(price)
      labels.push('0')
    }
    if (lineChart.value) {
      lineChart.value.chart.update()
    }
  } catch (e) {
    console.error(e)
  }
  const seconds = dataPoints.length
  const minutes = seconds / 60
  for(let data in dataPoints) {
    fetchAveragePrice(minutes, seconds - data, 0, seconds - data)
  }
}

async function fetchAveragePrice(m: number, s: number, m1: number, s1: number) {

  const route = useRoute()
  const stockSymbol = route.params.symbol

  try {
    const response = await fetch(`/api/stock/average-price?symbol=${stockSymbol}&from=${getDateTimeByOffset(m, s)}&to=${getDateTimeByOffset(m1, s1)}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const averagePriceText = await response.text()
    averagePrice.value = parseFloat(averagePriceText)
    while(averagePriceData.length >= numDataPoints) {
      averagePriceData.shift()
    }
    averagePriceData.push(averagePrice.value)
  } catch (e) {
    console.error(e)
  }
}

function getDateTimeByOffset(m: number, s: number) {
  const now = new Date()
  now.setSeconds(now.getSeconds() - s)
  now.setMinutes(now.getMinutes() - m)
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

</script>

<style lang="scss">
@use "./style.scss";

.stock-detail-header {
  position: relative;
  display: flex;
  align-content: center;
  justify-content: space-between;
  padding-top: 3%;
}

.stock-detail-header-info {
  width: 40%;
}

.stock-detail-header-chart-box {
  flex: 1;
  width: 50%;
}

.stock-detail-header-chart {
  flex: 1;
  height: 80%;
}

.stock-detail-header-chart-buttons {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>

