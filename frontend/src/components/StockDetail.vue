<template>
  <div class="stock-detail">
    <h2>{{ stock.name }} - Detailansicht</h2>
    <p><strong>Symbol:</strong> {{ stock.symbol }}</p>
    <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>
    <p><strong>Beschreibung:</strong></p>
    <p>{{ stock.description }}</p>

    <div class="purchase-buttons">
      <button class="purchase-button" @click="purchase(stock.symbol)">Kaufen</button>
      <button class="purchase-button" @click="sell(stock.symbol)">Verkaufen</button>
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
import {ref} from 'vue';
import {useRoute, useRouter} from "vue-router";

const stock = ref({
  id: 1,
  name: 'Apple',
  symbol: 'US0378331005',
  amount: 2,
  currentValue: 1450.90,
  change: 33.39,
  changePercentage: 13.6,
  description: "Beschreibung ist toll"
})

const route = useRoute();
const router = useRouter()

const isin = route.params.isin;
console.log("ISIN", isin)

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
</style>

