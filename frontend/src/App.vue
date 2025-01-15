<template>
  <main>
    <header id="header">
      <li id="icon"></li>
      <nav  id="header-navigation">
          <li title="Depotübersicht">
            <RouterLink :to="`/depot-uebersicht/${investmentAccountId}`" class="buttons">Depotübersicht</RouterLink>
          </li>
          <li>
            <RouterLink :to="`/wertpapier-uebersicht/${investmentAccountId}`" class="buttons">Wertpapierübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/krypto-uebersicht" class="buttons">Kryptoübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/metall-uebersicht" class="buttons">Metallübersicht</RouterLink>
          </li>
          <li>
            <RouterLink :to="`/order-management-overview/${investmentAccountId}`" class="buttons">Orderübersicht</RouterLink>    <!--TODO replace the hardcoded investmentAccountId-->
          </li>

      </nav>
    </header>
    <router-view></router-view>
  </main>
</template>

<script setup lang="ts">
import {ref} from 'vue';

const investmentAccountId = ref(1)
</script>

<style>
.buttons::after {
  display: block;
  content: attr(title);
  font-weight: bold;
  height: 1px;
  color: transparent;
  overflow: hidden;
  visibility: hidden;
}
</style>
