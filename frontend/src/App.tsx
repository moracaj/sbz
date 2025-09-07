import { Link, Outlet } from 'react-router-dom'

export default function App() {
  return (
    <div style={{padding:16, fontFamily:'system-ui'}}>
      <nav style={{display:'flex', gap:12, marginBottom:16}}>
        <Link to="/login">Login</Link>
        <Link to="/register">Register</Link>
 
      </nav>
      <Outlet />
      <p style={{marginTop:24, fontSize:12, opacity:0.6}}>SBZ demo skeleton</p>
    </div>
  )
}
