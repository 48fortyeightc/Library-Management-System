<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import api from '../services/api';

const books = ref([]);
const keyword = ref('');
const loading = ref(false);
const error = ref('');
const editingId = ref(null);
const form = reactive({
  title: '',
  author: '',
  isbn: '',
  category: '',
  description: '',
  totalCopies: 1,
});

const filtered = computed(() => books.value);

const resetForm = () => {
  editingId.value = null;
  form.title = '';
  form.author = '';
  form.isbn = '';
  form.category = '';
  form.description = '';
  form.totalCopies = 1;
};

const loadBooks = async () => {
  loading.value = true;
  error.value = '';
  try {
    const { data } = await api.get('/books', { params: { q: keyword.value || undefined } });
    books.value = data;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const saveBook = async () => {
  loading.value = true;
  error.value = '';
  try {
    if (editingId.value) {
      await api.put(`/books/${editingId.value}`, form);
    } else {
      await api.post('/books', form);
    }
    await loadBooks();
    resetForm();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const editBook = (book) => {
  editingId.value = book.id;
  form.title = book.title;
  form.author = book.author;
  form.isbn = book.isbn;
  form.category = book.category;
  form.description = book.description;
  form.totalCopies = book.totalCopies;
};

const removeBook = async (id) => {
  if (!confirm('确认删除这本书吗？')) return;
  loading.value = true;
  error.value = '';
  try {
    await api.delete(`/books/${id}`);
    await loadBooks();
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(loadBooks);
</script>

<template>
  <div class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">馆藏</p>
        <h1>图书管理</h1>
        <p class="muted">搜索、录入、维护馆藏信息。</p>
      </div>
      <div class="actions">
        <input v-model="keyword" class="input" placeholder="按书名或作者搜索" @keyup.enter="loadBooks" />
        <button class="btn ghost" @click="loadBooks">{{ loading ? '搜索中…' : '搜索' }}</button>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="grid two">
      <section class="panel">
        <div class="panel-head">
          <h3>{{ editingId ? '编辑图书' : '新增图书' }}</h3>
          <button v-if="editingId" class="btn ghost" @click="resetForm">取消</button>
        </div>
        <form class="form" @submit.prevent="saveBook">
          <label>
            <span>书名</span>
            <input v-model="form.title" required placeholder="请输入书名" />
          </label>
          <label>
            <span>作者</span>
            <input v-model="form.author" required placeholder="作者" />
          </label>
          <label>
            <span>ISBN</span>
            <input v-model="form.isbn" placeholder="ISBN" />
          </label>
          <label>
            <span>分类</span>
            <input v-model="form.category" placeholder="如：计算机" />
          </label>
          <label>
            <span>简介</span>
            <textarea v-model="form.description" rows="2" placeholder="补充说明"></textarea>
          </label>
          <label>
            <span>总册数</span>
            <input v-model.number="form.totalCopies" type="number" min="1" required />
          </label>
          <button class="btn primary" type="submit" :disabled="loading">
            {{ editingId ? '保存修改' : '创建图书' }}
          </button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>馆藏列表</h3>
          <span class="muted">{{ filtered.length }} 条</span>
        </div>
        <div v-if="loading" class="muted">加载中…</div>
        <div v-else-if="filtered.length === 0" class="muted">暂无图书。</div>
        <div v-else class="table">
          <div class="table-head">
            <span>书名</span>
            <span>分类</span>
            <span>库存</span>
            <span>操作</span>
          </div>
          <div v-for="book in filtered" :key="book.id" class="table-row">
            <span>
              <strong>{{ book.title }}</strong>
              <small class="muted block">{{ book.author }}</small>
            </span>
            <span>{{ book.category || '—' }}</span>
            <span>{{ book.availableCopies }}/{{ book.totalCopies }}</span>
            <span class="row-actions">
              <button class="link" @click="editBook(book)">编辑</button>
              <button class="link danger" @click="removeBook(book.id)">删除</button>
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
