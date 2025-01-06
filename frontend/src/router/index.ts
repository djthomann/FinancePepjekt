import { createRouter, createWebHistory } from 'vue-router'
import DepotUebersicht from '@/components/PortfolioOverview.vue'
import WertpapierUebersicht from '@/components/StockOverview.vue'
import WertpapierDetail from '@/components/StockDetail.vue'
import OrderManagementSell from "@/components/OrderManagementSell.vue";
import OrderManagementBuy from "@/components/OrderManagementBuy.vue";
import OrderManagementOverview from "@/components/OrderManagementOverview.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/depot-uebersicht',
      name: 'depot-uebersicht',
      component: DepotUebersicht,
    },
    {
      path: '/wertpapier-uebersicht',
      name: 'wertpapier-uebersicht',
      component: WertpapierUebersicht,
    },
    {
      path: '/wertpapier-detail/:symbol',
      name: 'wertpapier-detail',
      component: WertpapierDetail,
    },
    {
      path: '/order-management-sell/:isin',
      name: 'order-management-sell',
      component: OrderManagementSell,
    },
    {
      path: '/order-management-buy/:isin',
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
        return '/depot-uebersicht'
      },
    },
  ],
})

export default router
