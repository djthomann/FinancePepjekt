<template>
  <main>
    <header>
      <nav  id="header-navigation">
        <ul id="user">
          <li id="icon"></li>
          <li>
            <RouterLink :to="`/depot-uebersicht/${investmentAccountId}`" class="buttons">Depotübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/wertpapier-uebersicht" class="buttons">Wertpapierübersicht</RouterLink>
          </li>
          <li>
            <RouterLink :to="`/order-management-overview/${investmentAccountId}`" class="buttons">Orderübersicht</RouterLink>    <!--TODO replace the hardcoded investmentAccountId-->
          </li>
        </ul>
      </nav>
    </header>
    <router-view></router-view>
  </main>
</template>

<script setup lang="ts">
import {ref} from 'vue';

const investmentAccountId = ref(1)
</script>
