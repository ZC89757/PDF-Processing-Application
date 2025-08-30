import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Reader from '../pages/Reader.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  },
  {
    path: '/reader/:fileId',
    name: 'reader',
    component: Reader,
    props: route => ({ 
      page: parseInt(route.query.page) || 1,
      fileId: route.params.fileId
    })
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router