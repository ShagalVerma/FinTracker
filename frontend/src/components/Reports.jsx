import { useState } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import * as api from '../api'

function fmt(n) { return '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 }) }

export default function Reports() {
  const today      = new Date().toISOString().slice(0, 10)
  const monthStart = today.slice(0, 7) + '-01'

  const [from,    setFrom]    = useState(monthStart)
  const [to,      setTo]      = useState(today)
  const [report,  setReport]  = useState(null)
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')

  async function fetchReport() {
    setError(''); setLoading(true)
    try { setReport(await api.getReportJson(from, to)) }
    catch (err) { setError(err.message) }
    setLoading(false)
  }

  async function handleDownload() {
    try { await api.downloadReportCsv(from, to) }
    catch (err) { setError(err.message) }
  }

  // Build chart data from categoryBreakdown
  const barData = report
    ? Object.entries(report.categoryBreakdown || {}).map(([name, value]) => ({ name, Expense: value }))
    : []

  return (
    <div>
      <div className="page-header">
        <h1>📄 Reports</h1>
        <p>Analyse your finances over any date range and export to CSV.</p>
      </div>

      {/* Filter */}
      <div className="card">
        <div className="card-title">Date Range</div>
        <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div className="form-group">
            <label>From</label>
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
          </div>
          <div className="form-group">
            <label>To</label>
            <input type="date" value={to} onChange={e => setTo(e.target.value)} />
          </div>
          <button className="btn btn-primary" onClick={fetchReport} disabled={loading}>
            {loading ? 'Generating…' : '🔍 Generate Report'}
          </button>
          {report && (
            <button className="btn btn-outline" onClick={handleDownload}>
              ⬇ Download CSV
            </button>
          )}
        </div>
        {error && <div className="alert alert-danger" style={{ marginTop: 12 }}>{error}</div>}
      </div>

      {report && (
        <>
          {/* Summary cards */}
          <div className="stat-grid">
            <div className="stat-card">
              <div className="label">Total Income</div>
              <div className="value" style={{ color: 'var(--primary)' }}>{fmt(report.totalIncome)}</div>
            </div>
            <div className="stat-card red">
              <div className="label">Total Expense</div>
              <div className="value">{fmt(report.totalExpense)}</div>
            </div>
            <div className="stat-card blue">
              <div className="label">Net Balance</div>
              <div className="value">{fmt(report.netBalance)}</div>
            </div>
          </div>

          <div className="two-col">
            {/* Bar chart */}
            <div className="card">
              <div className="card-title">Expense by Category</div>
              {barData.length === 0
                ? <div className="empty-state"><div className="icon">📊</div>No expense data.</div>
                : (
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart data={barData} margin={{ top: 5, right: 20, left: 10, bottom: 40 }}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" angle={-35} textAnchor="end" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} tickFormatter={v => `₹${v}`} />
                      <Tooltip formatter={v => fmt(v)} />
                      <Bar dataKey="Expense" fill="#e74c3c" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )
              }
            </div>

            {/* Category table */}
            <div className="card">
              <div className="card-title">Category Breakdown</div>
              {Object.keys(report.categoryBreakdown || {}).length === 0
                ? <div className="empty-state"><div className="icon">📋</div>No expense data.</div>
                : (
                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr><th>Category</th><th>Amount</th><th>% of Total</th></tr>
                      </thead>
                      <tbody>
                        {Object.entries(report.categoryBreakdown).map(([cat, amt]) => (
                          <tr key={cat}>
                            <td>{cat}</td>
                            <td style={{ fontWeight: 600, color: 'var(--danger)' }}>{fmt(amt)}</td>
                            <td style={{ color: 'var(--muted)' }}>
                              {report.totalExpense > 0 ? ((amt / report.totalExpense) * 100).toFixed(1) + '%' : '—'}
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

          {/* Report meta */}
          <div className="card" style={{ padding: '14px 24px' }}>
            <p style={{ fontSize: '.85rem', color: 'var(--muted)' }}>
              Report for <strong>{report.username}</strong> · Period: <strong>{report.dateRange}</strong> · Generated: {report.generatedAt}
            </p>
          </div>
        </>
      )}

      {!report && !loading && (
        <div className="card">
          <div className="empty-state">
            <div className="icon">📄</div>
            <p>Select a date range and click <strong>Generate Report</strong> to view your financial summary.</p>
          </div>
        </div>
      )}
    </div>
  )
}
