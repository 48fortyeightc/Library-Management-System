<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { useSession } from './composables/useSession';

const { user, logout } = useSession();
const router = useRouter();
const roleLabel = (role) => {
  const map = { ADMIN: '管理员', LIBRARIAN: '馆员', STUDENT: '学生' };
  return map[role] || role;
};

const doLogout = () => {
  logout();
  router.push('/login');
};
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand" @click="router.push('/')">
        <div class="logo-dot"></div>
        <div>
          <p class="eyebrow">校园</p>
          <strong>图书管理</strong>
        </div>
      </div>
      <nav v-if="user" class="nav">
        <RouterLink to="/">概览</RouterLink>
        <RouterLink to="/books">图书</RouterLink>
        <RouterLink to="/loans">借阅</RouterLink>
        <RouterLink to="/users">用户</RouterLink>
      </nav>
      <div class="session">
        <template v-if="user">
          <div class="user-chip">
            <span class="avatar">{{ user.fullName?.[0] || user.username?.[0] }}</span>
            <div>
              <strong>{{ user.fullName }}</strong>
              <small class="muted block">{{ roleLabel(user.role) }}</small>
            </div>
          </div>
          <button class="btn ghost" @click="doLogout">退出</button>
        </template>
        <template v-else>
          <RouterLink class="btn primary" to="/login">登录</RouterLink>
        </template>
      </div>
    </header>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>
