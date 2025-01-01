<template>
      <div>
            <h2>{{ stock.name }} - Detailansicht</h2>
            <p><strong>ISIN:</strong> {{ stock.isin }}</p>
            <p><strong>Aktueller Wert:</strong> {{ stock.currentValue }} €</p>
            <p><strong>Beschreibung:</strong></p>
            <p>{{ stock.description }}</p>

            <div class="purchase-section">
                  <button class="purchase-button" @click="purchase">Kaufen</button>
                  <button class="purchase-button" @click="sell">Verkaufen</button>
            </div>

            <div>
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

            <div>
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
import { ref } from 'vue';

const stock = ref({ id: 1, name: 'Apple', isin: 'US0378331005', amount: 2, currentValue: 1450.90, change: 33.39, changePercentage: 13.6, description: "Beschreibung ist toll" })

function sell(){
      console.log('Verkaufen')
}

function purchase(){
      console.log('Kaufen')
}

</script>