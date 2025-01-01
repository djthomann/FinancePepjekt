<template>
      <div>
            <h2>{{ stock.name }} - Detailansicht</h2>
            <p><strong>ISIN:</strong> {{ stock.isin }}</p>
            <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>

            <div>
                  <form @submit.prevent="purchase">
                        <label for="amount">Anzahl</label>
                        <input type="number" id="amount" />
                        oder
                        <label for="sum">Summe</label>
                        <input type="number" id="sum" />
                        <br>
                        <label for="timestamp">Zeitpunkt</label>
                        <input type="date" id="timestamp" />

                        <button type="submit" class="purchase-button">Kaufen</button>
                  </form>
            </div>
      </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

const stock = ref({ id: 1, name: 'Apple', isin: 'US0378331005', amount: 2, currentValue: 1450.90, change: 33.39, changePercentage: 13.6, description: "Beschreibung ist toll" })

function purchase() {
      console.log('Kaufen')
}

</script>