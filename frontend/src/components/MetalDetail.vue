<template>
  <div class="stock-detail">
    <div class="stock-detail-header">
      <div class="stock-detail-header-info">
        <h2>{{ metal.name }} - Detailansicht</h2>
        <p><strong>Symbol:</strong> {{ metal.symbol }}</p>
        <p><strong>Aktueller Wert:</strong> {{ metal.currentPrice }} $</p>

        <div class="purchase-buttons">
          <button class="purchase-button" @click="purchase(metal.symbol)">Kaufen</button>
          <button class="purchase-button" @click="sell(metal.symbol)">Verkaufen</button>
        </div>
      </div>
      <div class="stock-detail-header-chart">
        <Line ref="lineChart"  :data="data" :options="options" />
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
import {onBeforeMount, onUnmounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import {type Metal} from '@/types/types.ts'
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
const metal = ref<Metal>({})
let pollingIntervalID: number

async function poll() {

  console.log("polling")

  try {
    const response = await fetch(`/api/metal?symbol=${metal.value.symbol}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const metalData = await response.json() as Metal;
    if (metal.value.currentPrice !== metalData.currentPrice) {
      metal.value.currentPrice = metalData.currentPrice
      metal.value.justChanged = true

      setTimeout(() => {
        metal.value.justChanged = false;
      }, 200);
    }
    if(dataPoints.length >= numDataPoints) {
      dataPoints.shift();
      labels.shift();
    }
    dataPoints.push(metalData.currentPrice)
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

  const route = useRoute();

  const symbol = route.params.symbol;
  console.log("Symbol", symbol)

  try {
    const response = await fetch(`/api/metal?symbol=${symbol}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    metal.value = await response.json() as Metal;
  } catch (e) {
    console.error(e);
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
