import { useEffect, useState } from 'react'
import * as api from '../api'

const EXPENSE_CATEGORIES = [
  'Food','Rent','Transport','Shopping','Healthcare',
  'Entertainment','Utilities','Education','Other Expense'
]

function fmt(n) { return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 }) }

export default function Budget() {
  const [budgets,    setBudgets]    = useState([])
  const [form,       setForm]       = useState({
    category: 'Food',
    limitAmount: '',
    month: new Date().toISOString().slice(0, 7)
  })
  const [loading,    setLoading]    = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error,      setError]      = useState('')
  const [month,      setMonth]      = useState(new Date().toISOString().slice(0, 7))

  useEffect(() => { fetchData() }, [])

  async function fetchData() {
    setLoading(true)
    try { setBudgets(await api.getBudgets()) }
    catch (_) {}
    setLoading(false)
  }

  const set = field => e => setForm(f => ({ ...f, [field]: e.target.value }))

  async function handleSubmit(e) {
    e.preventDefault()
    setError(''); setSubmitting(true)
    try {
      await api.setBudget({ ...form, limitAmount: parseFloat(form.limitAmount) })
      setForm(f => ({ ...f, limitAmount: '' }))
      await fetchData()
    } catch (err) { setError(err.message) }
    setSubmitting(false)
  }

  async function handleDelete(id) {
    if (!confirm('Delete this budget?')) return
    try { await api.deleteBudget(id); await fetchData() } catch (_) {}
  }

  const visible = budgets.filter(b => b.month === month)

  const totalLimit = visible.reduce((s, b) => s + b.limitAmount, 0)
  const totalSpent = visible.reduce((s, b) => s + b.spent, 0)

  return (
    <div>
      <div className="page-header">
        <h1>🎯 Budgets</h1>
        <p>Set monthly spending limits and track your progress.</p>
      </div>

      <div className="two-col">
        {/* Add form */}
        <div className="card">
          <div className="card-title">Set Budget</div>
          {error && <div className="alert alert-danger">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Category</label>
              <select value={form.category} onChange={set('category')}>
                {EXPENSE_CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 12 }}>
              <label>Monthly Limit (₹)</label>
              <input
                type="number" min="1" step="0.01"
                value={form.limitAmount} onChange={set('limitAmount')}
                required placeholder="e.g. 5000"
              />
            </div>
            <div className="form-group" style={{ marginBottom: 16 }}>
              <label>Month</label>
              <input type="month" value={form.month} onChange={set('month')} required />
            </div>
            <button className="btn btn-primary" type="submit" disabled={submitting} style={{ width: '100%' }}>
              {submitting ? 'Saving…' : '💾 Save Budget'}
            </button>
          </form>
        </div>

        {/* Summary for selected month */}
        <div className="card">
          <div className="card-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>Overview</span>
            <input type="month" value={month} onChange={e => setMonth(e.target.value)}
              style={{ padding: '4px 8px', border: '1px solid var(--border)', borderRadius: 6, fontSize: '.85rem' }} />
          </div>
          <div style={{ display: 'flex', gap: 16, marginBottom: 20 }}>
            <div style={{ flex: 1, padding: 16, background: '#f7fafc', borderRadius: 8, textAlign: 'center' }}>
              <div style={{ fontSize: '.8rem', color: 'var(--muted)', marginBottom: 4 }}>TOTAL BUDGET</div>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: 'var(--info)' }}>{fmt(totalLimit)}</div>
            </div>
            <div style={{ flex: 1, padding: 16, background: '#f7fafc', borderRadius: 8, textAlign: 'center' }}>
              <div style={{ fontSize: '.8rem', color: 'var(--muted)', marginBottom: 4 }}>TOTAL SPENT</div>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color: totalSpent > totalLimit ? 'var(--danger)' : 'var(--primary)' }}>{fmt(totalSpent)}</div>
            </div>
          </div>

          {loading
            ? <div className="loading">Loading…</div>
            : visible.length === 0
            ? <div className="empty-state"><div className="icon">🎯</div>No budgets for {month}.</div>
            : visible.map(b => {
                const pct  = b.limitAmount > 0 ? Math.min((b.spent / b.limitAmount) * 100, 100) : 0
                const over = b.spent > b.limitAmount
                const warn = !over && pct >= 80
                return (
                  <div key={b.id} style={{ marginBottom: 18 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6, alignItems: 'center' }}>
                      <span style={{ fontWeight: 600 }}>{b.category}</span>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ fontSize: '.82rem', color: over ? 'var(--danger)' : 'var(--muted)' }}>
                          {fmt(b.spent)} / {fmt(b.limitAmount)}
                          {over && ' ⚠ OVER'}
                        </span>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}>✕</button>
                      </div>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div className="progress-bar-bg">
                        <div className={`progress-bar-fill ${over ? 'over' : warn ? 'warn' : ''}`} style={{ width: `${pct}%` }} />
                      </div>
                      <span style={{ fontSize: '.78rem', minWidth: 36, color: over ? 'var(--danger)' : 'var(--muted)' }}>
                        {pct.toFixed(0)}%
                      </span>
                    </div>
                  </div>
                )
              })
          }
        </div>
      </div>
    </div>
  )
}
