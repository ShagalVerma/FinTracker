import { useEffect, useState } from 'react'
import * as api from '../api'

const CATEGORIES = {
  INCOME:  ['Salary','Freelance','Investment','Gift','Other Income'],
  EXPENSE: ['Food','Rent','Transport','Shopping','Healthcare','Entertainment','Utilities','Education','Other Expense']
}

function fmt(n) { return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 }) }

const EMPTY = { type:'EXPENSE', category:'Food', amount:'', description:'', date: new Date().toISOString().slice(0,10) }

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [form,         setForm]         = useState(EMPTY)
  const [filter,       setFilter]       = useState('ALL')
  const [search,       setSearch]       = useState('')
  const [loading,      setLoading]      = useState(true)
  const [submitting,   setSubmitting]   = useState(false)
  const [error,        setError]        = useState('')

  useEffect(() => { fetchData() }, [])

  async function fetchData() {
    setLoading(true)
    try { setTransactions(await api.getTransactions()) }
    catch (_) {}
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
      await api.addTransaction({ ...form, amount: parseFloat(form.amount) })
      setForm(EMPTY)
      await fetchData()
    } catch (err) { setError(err.message) }
    setSubmitting(false)
  }

  async function handleDelete(id) {
    if (!confirm('Delete this transaction?')) return
    try { await api.deleteTransaction(id); await fetchData() }
    catch (_) {}
  }

  const visible = transactions.filter(t => {
    const matchType   = filter === 'ALL' || t.type === filter
    const matchSearch = !search || t.category.toLowerCase().includes(search.toLowerCase())
                                || (t.description||'').toLowerCase().includes(search.toLowerCase())
    return matchType && matchSearch
  })

  return (
    <div>
      <div className="page-header">
        <h1>💸 Transactions</h1>
        <p>Track your income and expenses.</p>
      </div>

      {/* Add form */}
      <div className="card">
        <div className="card-title">Add Transaction</div>
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleAdd}>
          <div className="form-row">
            <div className="form-group">
              <label>Type</label>
              <select value={form.type} onChange={set('type')}>
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
              </select>
            </div>
            <div className="form-group">
              <label>Category</label>
              <select value={form.category} onChange={set('category')}>
                {CATEGORIES[form.type].map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Amount (₹)</label>
              <input type="number" min="0.01" step="0.01" value={form.amount} onChange={set('amount')} required placeholder="0.00" />
            </div>
            <div className="form-group">
              <label>Date</label>
              <input type="date" value={form.date} onChange={set('date')} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group" style={{gridColumn:'span 3'}}>
              <label>Description (optional)</label>
              <input value={form.description} onChange={set('description')} placeholder="e.g. Grocery shopping at DMart" />
            </div>
            <div className="form-group" style={{justifyContent:'flex-end'}}>
              <label>&nbsp;</label>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Adding…' : '+ Add'}
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Filter bar */}
      <div className="card" style={{padding:'16px 24px'}}>
        <div style={{display:'flex',gap:12,alignItems:'center',flexWrap:'wrap'}}>
          <div style={{display:'flex',gap:8}}>
            {['ALL','INCOME','EXPENSE'].map(f => (
              <button key={f} className={`btn btn-sm ${filter===f?'btn-primary':'btn-outline'}`} onClick={() => setFilter(f)}>
                {f}
              </button>
            ))}
          </div>
          <input
            placeholder="Search by category or description…"
            value={search} onChange={e => setSearch(e.target.value)}
            style={{flex:1, padding:'8px 12px', border:'1px solid var(--border)', borderRadius:6, fontSize:'.9rem'}}
          />
          <span style={{color:'var(--muted)',fontSize:'.85rem'}}>{visible.length} transactions</span>
        </div>
      </div>

      {/* Table */}
      <div className="card">
        {loading
          ? <div className="loading">Loading transactions…</div>
          : visible.length === 0
          ? <div className="empty-state"><div className="icon">📭</div>No transactions found.</div>
          : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Date</th><th>Type</th><th>Category</th>
                    <th>Description</th><th>Amount</th><th></th>
                  </tr>
                </thead>
                <tbody>
                  {visible.map(t => (
                    <tr key={t.id}>
                      <td>{t.date}</td>
                      <td><span className={`badge badge-${t.type.toLowerCase()}`}>{t.type}</span></td>
                      <td>{t.category}</td>
                      <td style={{color:'var(--muted)'}}>{t.description || '—'}</td>
                      <td style={{fontWeight:600, color: t.type==='INCOME'?'var(--primary)':'var(--danger)'}}>
                        {t.type==='INCOME'?'+':'-'}{fmt(t.amount)}
                      </td>
                      <td>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(t.id)}>✕</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        }
      </div>
    </div>
  )
}
