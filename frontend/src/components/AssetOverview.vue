<template>
  <div class="overview-flexbox">
    <div cla>
      <StockOverview></StockOverview>
    </div>
    <div>
      <CryptoOverview></CryptoOverview>
    </div>
    <div>
      <MetalOverview></MetalOverview>
    </div>
  </div>
</template>

<script setup lang="ts">

import CryptoOverview from "@/components/CryptoOverview.vue";
import MetalOverview from "@/components/MetalOverview.vue";
import StockOverview from "@/components/StockOverview.vue";
</script>

<style scoped lang="scss">
.overview-flexbox {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1fr;
  grid-column-gap: 0px;
  grid-row-gap: 0px;

}
</style>
