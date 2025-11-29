<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import api from '../services/api';
import { useSession } from '../composables/useSession';

const router = useRouter();
const route = useRoute();
const { setSession } = useSession();

const form = reactive({
  username: '',
  password: '',
});
const loading = ref(false);
const error = ref('');

const onSubmit = async () => {
  error.value = '';
  loading.value = true;
  try {
    const { data } = await api.post('/auth/login', form);
    setSession(data);
    router.push(route.query.redirect || '/');
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="page page-centered">
    <div class="card login-card">
      <div class="headline">
        <p class="eyebrow">校园图书馆</p>
        <h1>欢迎回来</h1>
        <p class="muted">登录以管理图书、读者与借阅。</p>
      </div>
      <form class="form" @submit.prevent="onSubmit">
        <label>
          <span>用户名</span>
          <input v-model="form.username" type="text" autocomplete="username" required placeholder="如：admin" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" required placeholder="请输入密码" />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="btn primary" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
        <p class="muted tip">演示账号：管理员 admin/admin123，馆员 librarian/lib123，学生 student/student123</p>
      </form>
    </div>
  </div>
  <div class="backdrop">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
  </div>
</template>
