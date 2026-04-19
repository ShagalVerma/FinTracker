import { createContext, useContext, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './components/Login'
import Signup from './components/Signup'
import Layout from './components/Layout'
import Dashboard from './components/Dashboard'
import Transactions from './components/Transactions'
import Budget from './components/Budget'
import Reports from './components/Reports'
import Recurring from './components/Recurring'

// ── Auth Context ──────────────────────────────────────────────────────────────
export const AuthContext = createContext(null)

export function useAuth() { return useContext(AuthContext) }

function AuthProvider({ children }) {
  const stored = localStorage.getItem('user')
  const [user, setUser] = useState(stored ? JSON.parse(stored) : null)

  function login(userData) {
    localStorage.setItem('user',  JSON.stringify(userData))
    localStorage.setItem('token', userData.token)
    setUser(userData)
  }

  function logout() {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// ── Protected route wrapper ───────────────────────────────────────────────────
function PrivateRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

// ── App ───────────────────────────────────────────────────────────────────────
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login"  element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
            <Route index           element={<Dashboard />} />
            <Route path="transactions" element={<Transactions />} />
            <Route path="budget"   element={<Budget />} />
            <Route path="reports"  element={<Reports />} />
            <Route path="recurring" element={<Recurring />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
