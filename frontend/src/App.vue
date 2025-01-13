<template>
  <main>
    <header id="header">
      <li id="icon"></li>
      <nav  id="header-navigation">
          <li title="Depotübersicht">
            <RouterLink to="/depot-uebersicht" class="buttons" title="Depotübersicht">Depotübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/wertpapier-uebersicht" class="buttons">Wertpapierübersicht</RouterLink>
          </li>
          <li>
            <RouterLink to="/order-management-overview/1" class="buttons">Orderübersicht</RouterLink>    <!--TODO replace the hardcoded investmentAccountId-->
          </li>

      </nav>
    </header>
    <router-view></router-view>
  </main>
</template>

<script setup lang="ts">
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
