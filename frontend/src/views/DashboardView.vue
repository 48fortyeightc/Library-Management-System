<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../services/api';

const router = useRouter();
const loading = ref(true);
const error = ref('');
const stats = reactive({
  books: 0,
  available: 0,
  totalCopies: 0,
  users: 0,
  loans: 0,
  overdue: 0,
});
const recentLoans = ref([]);

const statusLabel = (status) => {
  const map = {
    BORROWED: '借出',
    OVERDUE: '逾期',
    RETURNED: '已归还',
  };
  return map[status] || status;
};

const borrowRate = () => {
  const borrowed = Math.max(stats.totalCopies - stats.available, 0);
  if (!stats.totalCopies) return 0;
  return Math.min(Math.round((borrowed / stats.totalCopies) * 100), 100);
};

const overdueRate = () => {
  if (!stats.loans) return 0;
  return Math.min(Math.round((stats.overdue / stats.loans) * 100), 100);
};

const loadData = async () => {
  error.value = '';
  loading.value = true;
  try {
    const [booksRes, usersRes, loansRes] = await Promise.all([
      api.get('/books'),
      api.get('/users'),
      api.get('/loans'),
    ]);
    const books = booksRes.data || [];
    const users = usersRes.data || [];
    const loans = loansRes.data || [];
    stats.books = books.length;
    stats.totalCopies = books.reduce((sum, b) => sum + (b.totalCopies || 0), 0);
    stats.available = books.reduce((sum, b) => sum + (b.availableCopies || 0), 0);
    stats.users = users.length;
    stats.loans = loans.length;
    stats.overdue = loans.filter((l) => l.status === 'OVERDUE').length;
    recentLoans.value = loans.slice(-5).reverse();
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
        <p class="eyebrow">总览</p>
        <h1>图书馆仪表盘</h1>
        <p class="muted">快速了解馆藏、借阅与活跃度。</p>
      </div>
      <div class="actions">
        <button class="btn ghost" @click="loadData" :disabled="loading">{{ loading ? '刷新中…' : '刷新' }}</button>
        <button class="btn primary" @click="router.push('/books')">新增图书</button>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="grid stats">
      <div class="stat-card">
        <p class="label">图书</p>
        <h2>{{ stats.books }}</h2>
        <p class="muted">目录总册</p>
      </div>
      <div class="stat-card">
        <p class="label">可借副本</p>
        <h2>{{ stats.available }}</h2>
        <p class="muted">当前可借</p>
      </div>
      <div class="stat-card">
        <p class="label">读者</p>
        <h2>{{ stats.users }}</h2>
        <p class="muted">活跃账号</p>
      </div>
      <div class="stat-card">
        <p class="label">借阅</p>
        <h2>{{ stats.loans }}</h2>
        <p class="muted">累计借出</p>
      </div>
      <div class="stat-card" :class="{ warning: stats.overdue > 0 }">
        <p class="label">逾期</p>
        <h2>{{ stats.overdue }}</h2>
        <p class="muted">需跟进</p>
      </div>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div>
          <p class="eyebrow">仪表盘</p>
          <h3>馆藏与借阅概况</h3>
        </div>
      </div>
      <div class="grid two">
        <div class="meter-card">
          <div class="meter-top">
            <div>
              <p class="label">借出占比</p>
              <h2>{{ borrowRate() }}%</h2>
              <p class="muted">总册 {{ stats.totalCopies }} · 可借 {{ stats.available }}</p>
            </div>
          </div>
          <div class="progress">
            <div class="progress-bar" :style="{ width: borrowRate() + '%' }"></div>
          </div>
        </div>
        <div class="meter-card">
          <div class="meter-top">
            <div>
              <p class="label">逾期占比</p>
              <h2>{{ overdueRate() }}%</h2>
              <p class="muted">逾期 {{ stats.overdue }} / 借阅 {{ stats.loans }}</p>
            </div>
          </div>
          <div class="progress warning">
            <div class="progress-bar warning" :style="{ width: overdueRate() + '%' }"></div>
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <p class="eyebrow">动态</p>
          <h3>最近借阅</h3>
        </div>
        <button class="btn ghost" @click="router.push('/loans')">查看全部</button>
      </div>
      <div v-if="recentLoans.length === 0" class="muted">暂时没有借阅记录。</div>
      <div v-else class="table">
        <div class="table-head">
          <span>用户</span>
          <span>图书</span>
          <span>状态</span>
          <span>到期</span>
        </div>
        <div v-for="loan in recentLoans" :key="loan.id" class="table-row">
          <span>{{ loan.user?.fullName || loan.user?.username }}</span>
          <span>{{ loan.book?.title }}</span>
          <span>
            <span class="tag" :class="loan.status.toLowerCase()">{{ statusLabel(loan.status) }}</span>
          </span>
          <span>{{ loan.dueDate }}</span>
        </div>
      </div>
    </section>
  </div>
</template>
