<template>
  <main>
    <header>
      <nav  id="header-navigation">
        <ul id="user">
          <li id="icon"></li>
          <li>
            <RouterLink to="/depot-uebersicht" class="buttons">Depotübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/wertpapier-uebersicht" class="buttons">Wertpapierübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/wertpapier-detail" class="buttons">Wertpapier Detail</RouterLink>
          </li>
          <li>
            <RouterLink to="/order-management" class="buttons">Order Management</RouterLink>
          </li>
        </ul>
      </nav>
    </header>
    <router-view></router-view>
  </main>
</template>

<script setup lang="ts">
</script>
