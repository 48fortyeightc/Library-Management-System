import { computed, reactive } from 'vue';
import { setAuthToken } from '../services/api';

const saved = JSON.parse(localStorage.getItem('lms_session') || 'null');

const state = reactive({
  user: saved?.user || null,
  token: saved?.token || null,
});

if (state.token) {
  setAuthToken(state.token);
}

function persist() {
  localStorage.setItem('lms_session', JSON.stringify({ user: state.user, token: state.token }));
}

export function useSession() {
  const user = computed(() => state.user);
  const token = computed(() => state.token);
  const isAuthed = computed(() => !!state.user);

  function setSession(payload) {
    state.user = payload;
    state.token = payload?.token || null;
    persist();
    setAuthToken(state.token);
  }

  function logout() {
    state.user = null;
    state.token = null;
    persist();
    setAuthToken(null);
  }

  return { user, token, isAuthed, setSession, logout };
}
