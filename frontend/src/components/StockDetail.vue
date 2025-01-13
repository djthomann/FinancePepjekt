<template>
  <div class="stock-detail">
    <div class="stock-detail-header">
      <div class="stock-detail-header-info">
        <h2>{{ stock.name }} - Detailansicht</h2>
        <p><strong>Symbol:</strong> {{ stock.symbol }}</p>
        <p><strong>FIGI:</strong> {{ stock.figi }}</p>
        <p><strong>Aktueller Wert:</strong> {{ stock.cprice }} {{stock.currency}}</p>
        <p :class="{ 'just-changed': stock.justChanged}"><strong>Aktueller Wert:</strong> {{ stock.cprice }} {{stock.currency}}</p>
        <p><strong>Beschreibung:</strong> {{ stock.description }}</p>

        <div class="purchase-buttons">
          <button class="purchase-button" @click="purchase(stock.symbol)">Kaufen</button>
          <button class="purchase-button" @click="sell(stock.symbol)">Verkaufen</button>
        </div>
      </div>
      <div class="stock-detail-header-chart">
        <Line ref="lineChart"  :data="data" :options="options" />
      </div>
    </div>

    <div class="stock-info-section">
      <h2>Im Besitz</h2>
      <table>
        <tbody>
        <tr>
          <td>{{ stock.amount }} Stück</td>
          <td>{{ stock.currentValue * stock.amount }} €</td>
        </tr>
        <tr>
          <td>Seit Kauf</td>
          <td :class="{ 'positive': stock.change >= 0, 'negative': stock.change < 0 }">
            {{ stock.change }} € ({{ stock.changePercentage }}%)
          </td>
        </tr>
        <tr>
          <td>Kaufpreis gesamt</td>
          <td>{{ stock.currentValue * stock.amount }} €</td>
        </tr>
        <tr>
          <td>Kaufpreis Stück</td>
          <td>{{ stock.currentValue }} €</td>
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
          <td>{{ stock.currentValue }} €</td>
        </tr>
        <tr>
          <td>Tageshoch</td>
          <td>{{ stock.currentValue }} €</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import type { Stock} from '@/types/types.ts'
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

const dataPoints = []

const data = {
  labels: ['-18s', '-15s', '-12s', '9s', '-6s', '-3s', 'jetzt'],
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

const stock = ref<Stock>({})
let pollingIntervalID: number

async function poll() {

  console.log("polling")

  try {
    const response = await fetch(`/api/stock/by/symbol?symbol=${stock.value.symbol}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const stockData = await response.json() as Stock;
    if(stock.value.cprice !== stockData.cprice) {
      stock.value.cprice = stockData.cprice
      stock.value.justChanged = true
      setTimeout(() => {
        stock.value.justChanged = false;
      }, 200);
    }
    if(dataPoints.length > 6) {
      dataPoints.shift();
    }
    dataPoints.push(stockData.cprice)
    console.log("Data Points" + dataPoints);
    if (lineChart.value) {
      console.log("Instanz aktualisieren")
      lineChart.value.chart.update();
    } else {
      console.log("Instanz ist null")
    }
  } catch (e) {
    console.error(e);
  }

}

onBeforeMount(async () => {

  const route = useRoute();

  const symbol = route.params.symbol;
  console.log("Symbol", symbol)

  try {
    const response = await fetch(`/api/stock-details/symbol?symbol=${symbol}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stock.value = await response.json() as Stock
    console.log(stock.value)
  } catch (e) {
    console.error(e)
  }

  pollingIntervalID = setInterval(poll, 3000)
})

onUnmounted( () => {
  console.log("Clearing interval for polling")
  clearInterval(pollingIntervalID)
})

const router = useRouter()

function sell(symbol: string) {
  console.log('Verkaufen')
  router.push({name: 'order-management-sell', params: {symbol}});
}

function purchase(symbol: string) {
  console.log('Kaufen')
  router.push({name: 'order-management-buy', params: {symbol}});
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

