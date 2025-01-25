<template>
  <div class="stock-detail">
    <div class="stock-detail-header">
      <div class="stock-detail-header-info">
        <h2>{{ coin.name }} - Detailansicht</h2>
        <p><strong>Symbol:</strong> {{ coin.symbol }}</p>
        <p><strong>Aktueller Wert:</strong> {{ coin.currentPrice }} $</p>

        <div class="purchase-buttons">
          <button class="purchase-button" @click="purchase(coin.symbol)">Kaufen</button>
          <button class="purchase-button" @click="sell(coin.symbol)">Verkaufen</button>
        </div>
      </div>
      <div class="stock-detail-header-chart">
        <Line ref="lineChart"  :data="data" :options="options" />
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
import {onBeforeMount, onUnmounted, ref} from 'vue'
import {useRoute, useRouter} from "vue-router"
import {Coin, type Stock} from '@/types/types.ts'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

const numDataPoints = 60
const dataPoints = []
const labels = []

const data = {
  labels: labels,
  datasets: [
    {
      label: '',
      backgroundColor: 'transparent',
      borderColor: 'grey',
      borderWidth: 1,
      pointBorderWidth: 0,
      stepped: false,
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
const coin = ref<Coin>({})
let pollingIntervalID: number

async function poll() {

  console.log("polling")

  try {
    const response = await fetch(`/api/crypto?symbol=${coin.value.symbol}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const coinData = await response.json() as Coin
    if(coin.value.currentPrice !== coinData.currentPrice) {
      coin.value.currentPrice = coinData.currentPrice
      coin.value.justChanged = true
      setTimeout(() => {
        coin.value.justChanged = false
      }, 200)
    }
    if(dataPoints.length >= numDataPoints) {
      dataPoints.shift()
      labels.shift()
    }
    dataPoints.push(coinData.currentPrice)
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

  const symbol = route.params.symbol
  console.log("Symbol", symbol)

  try {
    const response = await fetch(`/api/crypto?symbol=${symbol}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    coin.value = await response.json() as Coin
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 1000)
})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const router = useRouter()

function sell(symbol: string) {
  console.log('Verkaufen')

}

function purchase(symbol: string) {
  console.log('Kaufen')
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
