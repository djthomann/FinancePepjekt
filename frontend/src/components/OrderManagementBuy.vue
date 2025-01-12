<template>
      <div class="stock-purchase">
            <h2>{{ stock.name }} - Detailansicht</h2>
            <p><strong>ISIN:</strong> {{ stock.isin }}</p>
            <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>

            <div>
                  <form @submit.prevent="purchase">
                        <label for="amount">Anzahl</label>
                        <input v-model.number="amount" type="number" id="amount" />
                        oder
                        <label for="sum">Summe</label>
                        <input type="number" id="sum" />
                        <br>
                        <label for="timestamp">Zeitpunkt</label>
                        <input v-model="date" type="date" id="timestamp" />

                        <button type="submit" class="purchase-button">Kaufen</button>
                  </form>
            </div>

      </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import {useRoute} from "vue-router";

const stock = ref({ id: 1, symbol:"AAPL", name: 'Apple', isin: 'US0378331005', amount: 2, currentValue: 1450.90, change: 33.39, changePercentage: 13.6, description: "Beschreibung ist toll" })

const route = useRoute();
const isin = route.params.isin;
console.log("ISIN", isin)

const amount = ref(10);

const url = "/api/buy/stock"

async function purchase() {
  console.log(amount.value + ' Stück kaufen zum Zeitpunkt: ' + date.value)
  const curl = url + `?investmentAccountId=1&stockSymbol=${stock.value.symbol}&volume=${amount.value}&executionTime=${date.value}`
  console.log(curl)
  try {
    const response = await fetch(curl, {
      method: "POST"
    });

    if (!response.ok) {
      throw new Error("Network response was not ok");
    }
    console.log("Success: Buy Order Placed");
  } catch (error) {
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
