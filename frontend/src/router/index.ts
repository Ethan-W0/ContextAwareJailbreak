import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/pages/Home.vue'),
    },
    {
      path: '/workshop',
      name: 'workshop',
      component: () => import('@/pages/Workshop.vue'),
    },
    {
      path: '/workshop/:sessionId',
      name: 'workshop-session',
      component: () => import('@/pages/WorkshopSession.vue'),
    },
    {
      path: '/console',
      name: 'console',
      component: () => import('@/pages/Console.vue'),
    },
    {
      path: '/console/:taskId',
      name: 'console-task',
      component: () => import('@/pages/TaskDetail.vue'),
    },
    {
      path: '/report/:taskId',
      name: 'report',
      component: () => import('@/pages/Report.vue'),
    },
    {
      path: '/vectors',
      name: 'vectors',
      component: () => import('@/pages/Vectors.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
});

export default router;
