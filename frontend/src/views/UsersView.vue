<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../services/api';

const users = ref([]);
const loading = ref(false);
const error = ref('');
const editingId = ref(null);
const form = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  role: 'STUDENT',
});

const roleOptions = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'LIBRARIAN', label: '馆员' },
  { value: 'STUDENT', label: '学生' },
];
const roleLabel = (role) => roleOptions.find((r) => r.value === role)?.label || role;

const resetForm = () => {
  editingId.value = null;
  form.username = '';
  form.password = '';
  form.fullName = '';
  form.email = '';
  form.role = 'STUDENT';
};

const loadUsers = async () => {
  loading.value = true;
  error.value = '';
  try {
    const { data } = await api.get('/users');
    users.value = data;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const saveUser = async () => {
  loading.value = true;
  error.value = '';
  try {
    if (editingId.value) {
      await api.put(`/users/${editingId.value}`, form);
    } else {
      await api.post('/users', form);
    }
    await loadUsers();
    resetForm();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const editUser = (user) => {
  editingId.value = user.id;
  form.username = user.username;
  form.password = '';
  form.fullName = user.fullName;
  form.email = user.email;
  form.role = user.role;
};

const deactivate = async (id) => {
  if (!confirm('确认停用该用户吗？')) return;
  loading.value = true;
  error.value = '';
  try {
    await api.delete(`/users/${id}`);
    await loadUsers();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(loadUsers);
</script>

<template>
  <div class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">人员</p>
        <h1>用户管理</h1>
        <p class="muted">维护管理员、馆员、学生账号。</p>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="grid two">
      <section class="panel">
        <div class="panel-head">
          <h3>{{ editingId ? '编辑用户' : '创建用户' }}</h3>
          <button v-if="editingId" class="btn ghost" @click="resetForm">取消</button>
        </div>
        <form class="form" @submit.prevent="saveUser">
          <label>
            <span>用户名</span>
            <input v-model="form.username" required />
          </label>
          <label>
            <span>密码</span>
            <input v-model="form.password" type="password" required placeholder="设置密码" />
          </label>
          <label>
            <span>姓名</span>
            <input v-model="form.fullName" required placeholder="姓名" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model="form.email" type="email" placeholder="选填" />
          </label>
          <label>
            <span>角色</span>
            <select v-model="form.role">
              <option v-for="role in roleOptions" :key="role.value" :value="role.value">{{ role.label }}</option>
            </select>
          </label>
          <button class="btn primary" type="submit" :disabled="loading">
            {{ editingId ? '保存修改' : '创建用户' }}
          </button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>用户列表</h3>
          <span class="muted">{{ users.length }} 个账号</span>
        </div>
        <div v-if="loading" class="muted">加载中…</div>
        <div v-else-if="users.length === 0" class="muted">暂无用户。</div>
        <div v-else class="table">
          <div class="table-head">
            <span>姓名</span>
            <span>角色</span>
            <span>状态</span>
            <span>操作</span>
          </div>
          <div v-for="user in users" :key="user.id" class="table-row">
            <span>
              <strong>{{ user.fullName }}</strong>
              <small class="muted block">{{ user.username }}</small>
            </span>
            <span>{{ roleLabel(user.role) }}</span>
            <span>
              <span class="tag" :class="user.active ? 'success' : 'danger'">{{ user.active ? '启用' : '停用' }}</span>
            </span>
            <span class="row-actions">
              <button class="link" @click="editUser(user)">编辑</button>
              <button class="link danger" @click="deactivate(user.id)">停用</button>
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
