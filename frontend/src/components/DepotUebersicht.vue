<template>
      <div>
            <div>
                  <div>
                        <h1>InvestDepot</h1>
                        <p>Hans Mustermann</p>
                  </div>
                  <div>
                        <p>Depotwert: <strong>{{ totalValue }} €</strong></p>
                  </div>
            </div>

            <h2>Positionen</h2>
            <table>
                  <thead>
                        <tr>
                              <th>Name</th>
                              <th>ISIN</th>
                              <th>Aktueller Wert</th>
                              <th>Anteile</th>
                              <th>Gewinn/Verlust</th>
                        </tr>
                  </thead>
                  <tbody>
                        <tr v-for="position in positions" :key="position.id">
                              <td>{{ position.name }}</td>
                              <td>{{ position.isin }}</td>
                              <td>{{ position.currentValue }} €</td>
                              <td>{{ position.amount }}</td>
                              <td :class="{ 'positive': position.change >= 0, 'negative': position.change < 0 }">
                                    {{ position.change }} € ({{ position.changePercentage }}%)
                              </td>
                        </tr>
                  </tbody>
            </table>
      </div>
</template>

<script lang="ts">
import { ref } from 'vue';

export default {
      setup() {
            const totalValue = ref(13834.26);
            const positions = ref([
                  { id: 1, name: 'Apple', isin: 10, amount: 2, currentValue: 1450.90, change: 33.39, changePercentage: 13.6 },
                  { id: 2, name: 'Tesla', isin: 5, amount: 2, currentValue: 3565.35, change: 120.75, changePercentage: 20.1 },
                  { id: 3, name: 'Amazon', isin: 2, amount: 2, currentValue: 6169.52, change: -45.50, changePercentage: -5.2 },
            ]);

            return {
                  totalValue,
                  positions
            };
      }
};

</script>