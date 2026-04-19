const BASE = '/api';

function getToken() {
  return localStorage.getItem('token');
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getToken()}`
  };
}

async function request(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : {};
  if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
  return data;
}

// ── Auth ──────────────────────────────────────────────────────────────────────
export const signup  = (u, e, p) => request('POST', '/auth/signup', { username: u, email: e, password: p });
export const login   = (u, p)    => request('POST', '/auth/login',  { username: u, password: p });
export const logout  = ()        => request('POST', '/auth/logout');

// ── Transactions ──────────────────────────────────────────────────────────────
export const getTransactions   = ()      => request('GET',    '/transactions');
export const addTransaction    = (data)  => request('POST',   '/transactions', data);
export const deleteTransaction = (id)    => request('DELETE', `/transactions/${id}`);

// ── Budgets ───────────────────────────────────────────────────────────────────
export const getBudgets   = ()     => request('GET',    '/budgets');
export const setBudget    = (data) => request('POST',   '/budgets', data);
export const deleteBudget = (id)   => request('DELETE', `/budgets/${id}`);

// ── Summary ───────────────────────────────────────────────────────────────────
export const getSummary = (month) => request('GET', `/summary?month=${month}`);

// ── Report ────────────────────────────────────────────────────────────────────
export const getReportJson = (from, to) => request('GET', `/report?from=${from}&to=${to}&format=json`);

export async function downloadReportCsv(from, to) {
  const res = await fetch(`${BASE}/report?from=${from}&to=${to}&format=csv`, {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });
  if (!res.ok) throw new Error('Failed to download report');
  const blob = await res.blob();
  const url  = URL.createObjectURL(blob);
  const a    = document.createElement('a');
  a.href     = url;
  a.download = `report_${from}_to_${to}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

// ── Recurring ─────────────────────────────────────────────────────────────────
export const getRecurring    = ()     => request('GET',    '/recurring');
export const addRecurring    = (data) => request('POST',   '/recurring', data);
export const deleteRecurring = (id)   => request('DELETE', `/recurring/${id}`);
