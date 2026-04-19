import { useEffect, useState } from 'react'
import * as api from '../api'

const CATEGORIES = {
  INCOME:  ['Salary','Freelance','Investment','Gift','Other Income'],
  EXPENSE: ['Food','Rent','Transport','Shopping','Healthcare','Entertainment','Utilities','Education','Other Expense']
}
const FREQUENCIES = ['DAILY', 'WEEKLY', 'MONTHLY']

function fmt(n) { return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 }) }

const EMPTY = {
  type: 'EXPENSE', category: 'Food', amount: '',
  description: '', frequency: 'MONTHLY',
  nextDate: new Date().toISOString().slice(0, 10)
}

export default function Recurring() {
  const [list,       setList]       = useState([])
  const [form,       setForm]       = useState(EMPTY)
  const [loading,    setLoading]    = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error,      setError]      = useState('')

  useEffect(() => { fetchData() }, [])

  async function fetchData() {
    setLoading(true)
    try { setList(await api.getRecurring()) } catch (_) {}
    setLoading(false)
  }

  const set = field => e => {
    const val = e.target.value
    setForm(f => {
      const next = { ...f, [field]: val }
      if (field === 'type') next.category = CATEGORIES[val][0]
      return next
    })
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError(''); setSubmitting(true)
    try {
      await api.addRecurring({ ...form, amount: parseFloat(form.amount) })
      setForm(EMPTY)
      await fetchData()
    } catch (err) { setError(err.message) }
    setSubmitting(false)
  }

  async function handleDelete(id) {
    if (!confirm('Delete this recurring transaction?')) return
    try { await api.deleteRecurring(id); await fetchData() } catch (_) {}
  }

  const freqLabel = { DAILY: '🔁 Daily', WEEKLY: '📅 Weekly', MONTHLY: '🗓 Monthly' }

  return (
    <div>
      <div className="page-header">
        <h1>🔄 Recurring Transactions</h1>
        <p>Automate regular income and expenses. They are applied automatically when their due date arrives.</p>
      </div>

      <div className="two-col">
        {/* Add form */}
        <div className="card">
          <div className="card-title">New Recurring Transaction</div>
          {error && <div className="alert alert-danger">{error}</div>}
          <form onSubmit={handleAdd}>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Type</label>
              <select value={form.type} onChange={set('type')}>
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Category</label>
              <select value={form.category} onChange={set('category')}>
                {CATEGORIES[form.type].map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Amount (₹)</label>
              <input type="number" min="0.01" step="0.01" value={form.amount}
                onChange={set('amount')} required placeholder="0.00" />
            </div>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Frequency</label>
              <select value={form.frequency} onChange={set('frequency')}>
                {FREQUENCIES.map(f => <option key={f}>{f}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>First Due Date</label>
              <input type="date" value={form.nextDate} onChange={set('nextDate')} required />
            </div>
            <div className="form-group" style={{ marginBottom: 16 }}>
              <label>Description (optional)</label>
              <input value={form.description} onChange={set('description')} placeholder="e.g. Monthly rent" />
            </div>
            <button className="btn btn-primary" type="submit" disabled={submitting} style={{ width: '100%' }}>
              {submitting ? 'Adding…' : '+ Add Recurring'}
            </button>
          </form>
        </div>

        {/* List */}
        <div className="card">
          <div className="card-title">Active Recurring ({list.length})</div>
          {loading
            ? <div className="loading">Loading…</div>
            : list.length === 0
            ? <div className="empty-state"><div className="icon">🔄</div>No recurring transactions yet.</div>
            : list.map(r => (
              <div key={r.id} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '14px 0', borderBottom: '1px solid var(--border)'
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <span className={`badge badge-${r.type.toLowerCase()}`}>{r.type}</span>
                    <strong>{r.category}</strong>
                    <span style={{ fontSize: '.8rem', background: '#eef2ff', color: '#4338ca', padding: '2px 8px', borderRadius: 12 }}>
                      {freqLabel[r.frequency]}
                    </span>
                  </div>
                  <div style={{ fontSize: '.85rem', color: 'var(--muted)' }}>
                    {r.description || '—'} · Next: <strong>{r.nextDate}</strong>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontWeight: 700, color: r.type === 'INCOME' ? 'var(--primary)' : 'var(--danger)' }}>
                    {r.type === 'INCOME' ? '+' : '-'}{fmt(r.amount)}
                  </span>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(r.id)}>✕</button>
                </div>
              </div>
            ))
          }
        </div>
      </div>
    </div>
  )
}
