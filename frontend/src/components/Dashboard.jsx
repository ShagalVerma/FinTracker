import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import * as api from '../api'
import { useAuth } from '../App'

const COLORS = ['#2ecc71','#3498db','#e74c3c','#f39c12','#9b59b6','#1abc9c','#e67e22','#34495e']

function fmt(n) {
  return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 })
}

export default function Dashboard() {
  const { user }    = useAuth()
  const [summary,   setSummary]   = useState(null)
  const [recents,   setRecents]   = useState([])
  const [budgets,   setBudgets]   = useState([])
  const [loading,   setLoading]   = useState(true)
  const [month,     setMonth]     = useState(new Date().toISOString().slice(0, 7))

  useEffect(() => { fetchAll() }, [month])

  async function fetchAll() {
    setLoading(true)
    try {
      const [s, txns, b] = await Promise.all([
        api.getSummary(month),
        api.getTransactions(),
        api.getBudgets()
      ])
      setSummary(s)
      setRecents(txns.slice(0, 5))
      setBudgets(b.filter(bgt => bgt.month === month))
    } catch (_) {}
    setLoading(false)
  }

  const pieData = summary
    ? Object.entries(summary.categoryBreakdown || {}).map(([name, value]) => ({ name, value }))
    : []

  const alerts = budgets.filter(b => b.spent > b.limitAmount)

  if (loading) return <div className="loading">Loading dashboard…</div>

  return (
    <div>
      <div className="page-header" style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
        <div>
          <h1>👋 Hello, {user?.username}!</h1>
          <p>Here's your financial overview.</p>
        </div>
        <div className="form-group" style={{ minWidth: 160 }}>
          <label>Month</label>
          <input type="month" value={month} onChange={e => setMonth(e.target.value)} />
        </div>
      </div>

      {/* Budget alerts */}
      {alerts.map(b => (
        <div key={b.id} className="alert alert-danger">
          ⚠ Budget exceeded for <strong>{b.category}</strong>: spent {fmt(b.spent)} of {fmt(b.limitAmount)} limit.
        </div>
      ))}

      {/* Stat cards */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="label">Total Income</div>
          <div className="value" style={{color:'var(--primary)'}}>{fmt(summary?.totalIncome ?? 0)}</div>
        </div>
        <div className="stat-card red">
          <div className="label">Total Expense</div>
          <div className="value">{fmt(summary?.totalExpense ?? 0)}</div>
        </div>
        <div className="stat-card blue">
          <div className="label">Net Balance</div>
          <div className="value">{fmt(summary?.netBalance ?? 0)}</div>
        </div>
        <div className="stat-card" style={{borderLeftColor:'var(--warning)'}}>
          <div className="label">Active Budgets</div>
          <div className="value" style={{color:'var(--warning)'}}>{budgets.length}</div>
        </div>
      </div>

      <div className="two-col">
        {/* Pie chart */}
        <div className="card">
          <div className="card-title">Expense Breakdown</div>
          {pieData.length === 0
            ? <div className="empty-state"><div className="icon">🍩</div>No expense data for this month.</div>
            : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label={({name,percent}) => `${name} ${(percent*100).toFixed(0)}%`}>
                    {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip formatter={v => fmt(v)} />
                </PieChart>
              </ResponsiveContainer>
            )
          }
        </div>

        {/* Budget overview */}
        <div className="card">
          <div className="card-title">Budget Status</div>
          {budgets.length === 0
            ? <div className="empty-state"><div className="icon">🎯</div>No budgets for this month. <Link to="/budget">Add one →</Link></div>
            : budgets.map(b => {
                const pct  = Math.min((b.spent / b.limitAmount) * 100, 100)
                const over = b.spent > b.limitAmount
                const warn = !over && pct >= 80
                return (
                  <div key={b.id} style={{marginBottom:16}}>
                    <div style={{display:'flex',justifyContent:'space-between',marginBottom:6}}>
                      <span style={{fontWeight:600}}>{b.category}</span>
                      <span style={{fontSize:'.85rem',color: over ? 'var(--danger)' : 'var(--muted)'}}>
                        {fmt(b.spent)} / {fmt(b.limitAmount)}
                      </span>
                    </div>
                    <div style={{display:'flex',alignItems:'center',gap:8}}>
                      <div className="progress-bar-bg">
                        <div
                          className={`progress-bar-fill ${over?'over':warn?'warn':''}`}
                          style={{width:`${pct}%`}}
                        />
                      </div>
                      <span style={{fontSize:'.78rem',minWidth:36}}>{pct.toFixed(0)}%</span>
                    </div>
                  </div>
                )
              })
          }
        </div>
      </div>

      {/* Recent transactions */}
      <div className="card">
        <div className="card-title" style={{display:'flex',justifyContent:'space-between'}}>
          <span>Recent Transactions</span>
          <Link to="/transactions" style={{fontSize:'.85rem',color:'var(--primary)'}}>View all →</Link>
        </div>
        {recents.length === 0
          ? <div className="empty-state"><div className="icon">💸</div>No transactions yet.</div>
          : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Date</th><th>Category</th><th>Description</th><th>Type</th><th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {recents.map(t => (
                    <tr key={t.id}>
                      <td>{t.date}</td>
                      <td>{t.category}</td>
                      <td style={{color:'var(--muted)'}}>{t.description || '—'}</td>
                      <td><span className={`badge badge-${t.type.toLowerCase()}`}>{t.type}</span></td>
                      <td style={{fontWeight:600, color: t.type==='INCOME'?'var(--primary)':'var(--danger)'}}>
                        {t.type==='INCOME'?'+':'-'}{fmt(t.amount)}
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
