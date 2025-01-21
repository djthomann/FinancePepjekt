<template>
  <div class="stock-detail">
    <h2>{{ stockDetail.stock.name }} - Detailansicht</h2>
    <p><strong>FIGI:</strong> {{ stockDetail.stock.figi }}</p>
    <p><strong>Aktueller Wert:</strong> {{ stockDetail.stock.latestQuote.currentPrice }} €</p>

    <div class="stock-info-section">
      <h2>Im Besitz</h2>
      <table>
        <tbody>
        <tr>
          <td>Stück</td>
          <td>{{ stockDetail.portfolioEntry.quantity }}</td>
        </tr>
        <tr>
          <td>Seit Kauf</td>
          <td :class="{ 'positive': stockDetail.stock.latestQuote.change >= 0, 'negative':
          stockDetail.stock.latestQuote.change
           < 0 }">
            {{ stockDetail.stock.latestQuote.change }} € ({{ stockDetail.stock.latestQuote.percentChange }}%)
          </td>
        </tr>
        <tr>
          <td>Kaufpreis gesamt</td>
          <td>{{ stockDetail.portfolioEntry.totalValue }} {{ stockDetail.stock.currency }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="stock-order">
      <div>
        <form @submit.prevent="sell">
          <label for="volume">Anteile</label>
          <input v-model.number="volume" type="number" id="volume" step="0.01" />
          <label for="timestamp-date">Datum</label>
          <input v-model="date" type="date" id="timestamp-date"/>
          <label for="timestamp-time">Uhrzeit</label>
          <input v-model="time" type="time" id="timestamp-time"/>

          <button type="submit" class="purchase-button">Verkaufen</button>
          <p>{{ serverResponse }}</p>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, ref} from 'vue';
import {useRoute} from "vue-router";
import type {Stock, StockDetails} from "@/types/types.ts";

const stockDetail = ref<StockDetails>({})

const volume = ref(0);
const serverResponse = ref<string>()

const date = ref(getTodayDate());
const time = ref("00:00");

const url = "/api/sell/stock"

onBeforeMount(async () => {
  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  try {
    const response = await
      fetch(`/api/stock-details/symbol?symbol=${stockSymbol}&investmentAccountId=${investmentAccountId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    stockDetail.value = await response.json() as StockDetails
  } catch (e) {
    console.error(e)
  }
})

async function sell() {
  const route = useRoute()
  const stockSymbol = route.params.symbol
  const investmentAccountId = route.params.investmentAccountId

  const executionTime = `${date.value}T${time.value}`;

  console.log(volume.value + ' Stück verkaufen zum Zeitpunkt: ' + executionTime)
  const baseUrl = "/api/placeSellOrder";
  const requestUrl =
    `${baseUrl}?investmentAccountId=${investmentAccountId}&stockSymbol=${stockSymbol}&volume=${volume.value}&executionTime=${executionTime}`;
  try {
    const response = await fetch(requestUrl, {
      method: "POST"
    });

    if (!response.ok) {
      serverResponse.value = "Error: Order invalid"
      throw new Error("Network response was not ok");
    }
    serverResponse.value = "Success: Sell Order Placed"
  } catch (error) {
    serverResponse.value = "Server Error: Order Not Placed"
    console.error("Error: Order Not Placed", error);
  }
}

function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

</script>

<style lang="scss">
@use "./style.scss";

</style>

