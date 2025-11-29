<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../services/api';

const loans = ref([]);
const users = ref([]);
const books = ref([]);
const loading = ref(false);
const error = ref('');

const form = reactive({
  userId: null,
  bookId: null,
  loanDays: 14,
  note: '',
});

const statusLabel = (status) => {
  const map = {
    BORROWED: '借出',
    OVERDUE: '逾期',
    RETURNED: '已归还',
  };
  return map[status] || status;
};

const loadData = async () => {
  loading.value = true;
  error.value = '';
  try {
    const [loanRes, userRes, bookRes] = await Promise.all([
      api.get('/loans'),
      api.get('/users'),
      api.get('/books'),
    ]);
    loans.value = loanRes.data;
    users.value = userRes.data.filter((u) => u.active);
    books.value = bookRes.data;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const createLoan = async () => {
  loading.value = true;
  error.value = '';
  try {
    await api.post('/loans', form);
    await loadData();
    form.userId = null;
    form.bookId = null;
    form.loanDays = 14;
    form.note = '';
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const returnLoan = async (id) => {
  loading.value = true;
  error.value = '';
  try {
    await api.post(`/loans/${id}/return`);
    await loadData();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);
</script>

<template>
  <div class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">流通</p>
        <h1>借阅管理</h1>
        <p class="muted">一站式发起借阅、归还与逾期跟进。</p>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="grid two">
      <section class="panel">
        <div class="panel-head">
          <h3>发起借阅</h3>
        </div>
        <form class="form" @submit.prevent="createLoan">
          <label>
            <span>借阅人</span>
            <select v-model.number="form.userId" required>
              <option disabled value="">选择用户</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.fullName }} ({{ user.username }})
              </option>
            </select>
          </label>
          <label>
            <span>图书</span>
            <select v-model.number="form.bookId" required>
              <option disabled value="">选择图书</option>
              <option v-for="book in books" :key="book.id" :value="book.id">
                {{ book.title }} — 可借 {{ book.availableCopies }} 本
              </option>
            </select>
          </label>
          <label>
            <span>借阅天数</span>
            <input v-model.number="form.loanDays" type="number" min="1" />
          </label>
          <label>
            <span>备注</span>
            <textarea v-model="form.note" rows="2" placeholder="可选填写"></textarea>
          </label>
          <button class="btn primary" type="submit" :disabled="loading">确认借出</button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>借阅记录</h3>
          <span class="muted">{{ loans.length }} 条</span>
        </div>
        <div v-if="loading" class="muted">加载中…</div>
        <div v-else-if="loans.length === 0" class="muted">暂无借阅。</div>
        <div v-else class="table">
          <div class="table-head">
            <span>用户</span>
            <span>图书</span>
            <span>状态</span>
            <span>到期 / 归还</span>
            <span>操作</span>
          </div>
          <div v-for="loan in loans" :key="loan.id" class="table-row">
            <span>
              <strong>{{ loan.user?.fullName || loan.user?.username }}</strong>
              <small class="muted block">{{ loan.user?.email }}</small>
            </span>
            <span>
              <strong>{{ loan.book?.title }}</strong>
              <small class="muted block">{{ loan.book?.author }}</small>
            </span>
            <span><span class="tag" :class="loan.status.toLowerCase()">{{ statusLabel(loan.status) }}</span></span>
            <span>{{ loan.returnDate || loan.dueDate }}</span>
            <span class="row-actions">
              <button class="link" :disabled="loan.status === 'RETURNED'" @click="returnLoan(loan.id)">归还</button>
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
