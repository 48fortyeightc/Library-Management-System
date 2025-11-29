import { createRouter, createWebHistory } from 'vue-router';
import DashboardView from '../views/DashboardView.vue';
import LoginView from '../views/LoginView.vue';
import BooksView from '../views/BooksView.vue';
import UsersView from '../views/UsersView.vue';
import LoansView from '../views/LoansView.vue';
import { useSession } from '../composables/useSession';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    { path: '/', name: 'dashboard', component: DashboardView },
    { path: '/books', name: 'books', component: BooksView },
    { path: '/users', name: 'users', component: UsersView },
    { path: '/loans', name: 'loans', component: LoansView },
  ],
});

router.beforeEach((to, from, next) => {
  const { user } = useSession();
  if (to.meta.public) {
    next();
    return;
  }
  if (!user.value) {
    next({ name: 'login', query: { redirect: to.fullPath } });
    return;
  }
  next();
});

export default router;
