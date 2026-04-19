import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../App'
import * as api from '../api'

const NAV = [
  { to: '/',            icon: '📊', label: 'Dashboard'    },
  { to: '/transactions',icon: '💸', label: 'Transactions'  },
  { to: '/budget',      icon: '🎯', label: 'Budgets'       },
  { to: '/recurring',   icon: '🔄', label: 'Recurring'     },
  { to: '/reports',     icon: '📄', label: 'Reports'       },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    try { await api.logout() } catch (_) {}
    logout()
    navigate('/login')
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">
          <span>💰</span> FinTracker
        </div>
        <nav>
          {NAV.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => isActive ? 'active' : ''}
            >
              <span>{icon}</span> {label}
            </NavLink>
          ))}
        </nav>
        <div className="user-info">
          <strong>{user?.username}</strong>
          {user?.email}
          <button className="logout-btn" onClick={handleLogout}>Sign out</button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
