<template>
      <div class="stock-order">
            <h2>{{ stock.name }} - Detailansicht</h2>
            <p><strong>FIGI:</strong> {{ stock.figi }}</p>
            <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>

            <div>
                  <form @submit.prevent="sell">
                        <label for="amount">Anzahl</label>
                        <input v-model.number="amount" type="number" id="amount" />
                        <label for="timestamp">Zeitpunkt</label>
                        <input v-model="date" type="date" id="timestamp" />

                        <button type="submit" class="purchase-button">Verkaufen</button>
                        <p>{{serverResponse}}</p>
                  </form>
            </div>
      </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, ref} from 'vue';
import {useRoute} from "vue-router";
import type {Stock} from "@/types/types.ts";

const stock = ref<Stock>({})

const amount = ref(0);
const serverResponse = ref<string>()

const url = "/api/sell/stock"

onBeforeMount(async () => {

  const route = useRoute();

  const symbol = route.params.symbol;

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
})

async function sell() {
  console.log(amount.value + ' Stück verkaufen zum Zeitpunkt: ' + date.value)
  const curl = url + `?investmentAccountId=1&stockSymbol=${stock.value.symbol}&volume=${amount.value}&executionTime=${date.value}`
  console.log(curl)
  try {
    const response = await fetch(curl, {
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

const getTodayDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0'); // Monat von 0-11, daher +1
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const date = ref(getTodayDate());

</script>

<style lang="scss">
@use "./style.scss";
</style>
