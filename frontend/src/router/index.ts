import { createRouter, createWebHistory } from 'vue-router'
import DepotUebersicht from '@/components/PortfolioOverview.vue'
import WertpapierUebersicht from '@/components/StockOverview.vue'
import WertpapierDetail from '@/components/StockDetail.vue'
import OrderManagementSell from "@/components/OrderManagementSell.vue";
import OrderManagementBuy from "@/components/OrderManagementBuy.vue";
import OrderManagementOverview from "@/components/OrderManagementOverview.vue";
import CryptoOverview from "@/components/CryptoOverview.vue";
import CryptoDetail from "@/components/CryptoDetail.vue";
import MetalOverview from "@/components/MetalOverview.vue";
import MetalDetail from "@/components/MetalDetail.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/depot-uebersicht/:investmentAccountId',
      name: 'depot-uebersicht',
      component: DepotUebersicht,
    },
    {
      path: '/wertpapier-uebersicht/:investmentAccountId',
      name: 'wertpapier-uebersicht',
      component: WertpapierUebersicht,
    },
    {
      path: '/wertpapier-detail/:symbol/:investmentAccountId',
      name: 'wertpapier-detail',
      component: WertpapierDetail,
    },
    {
      path: '/krypto-uebersicht',
      name: 'krypto-uebersicht',
      component: CryptoOverview,
    },
    {
      path: '/krypto-detail/:symbol',
      name: 'krypto-detail',
      component: CryptoDetail,
    },
    {
      path: '/metall-uebersicht',
      name: 'metall-uebersicht',
      component: MetalOverview,
    },
    {
      path: '/metall-detail/:symbol',
      name: 'metall-detail',
      component: MetalDetail,
    },
    {
      path: '/order-management-sell/:symbol',
      name: 'order-management-sell',
      component: OrderManagementSell,
    },
    {
      path: '/order-management-buy/:symbol/:investmentAccountId',
      name: 'order-management-buy',
      component: OrderManagementBuy,
    },
    {
      path: '/order-management-overview/:investmentAccountId',
      name: 'order-management-overview',
      component: OrderManagementOverview,
    },
    {
      path: '',
      redirect: (to: any) => {
        const { hash, params, query } = to
        return '/depot-uebersicht/1'
      },
    },
  ],
})

export default router
