import { createRouter, createWebHistory } from 'vue-router'
import DepotUebersicht from '@/components/PortfolioOverview.vue'
import WertpapierUebersicht from '@/components/StockOverview.vue'
import WertpapierDetail from '@/components/StockDetail.vue'
import OrderManagement from '@/components/OrderManagement.vue'

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
      path: '/wertpapier-detail',
      name: 'wertpapier-detail',
      component: WertpapierDetail,
    },
    {
      path: '/order-management',
      name: 'order-management',
      component: OrderManagement,
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
