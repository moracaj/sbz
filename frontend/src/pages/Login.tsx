import { useState } from 'react'
import { login } from '../api'
import { useNavigate } from 'react-router-dom'


export default function Login(){
  const [form, setForm] = useState({email:'', password:''})
  const [msg, setMsg] = useState<string|undefined>()
  const navigate = useNavigate()

  const submit = async (e:any) => {
    e.preventDefault()
    setMsg(undefined)
    try{
      const res = await login(form)
      localStorage.setItem('token', res.token)
      setMsg('Logged in! Token saved.')


      
      localStorage.setItem('token', res.token)
      setMsg('✅ Logged in! Token saved to localStorage.')
      navigate('/me/posts')   // <— preusmerenje
    }catch(err:any){ setMsg(err.message || 'Error') }
  }

  return (
    <form onSubmit={submit} style={{display:'grid', gap:8, maxWidth:360}}>
      <h2>Login</h2>
      <input placeholder="Email" value={form.email} onChange={e=>setForm(f=>({...f, email:e.target.value}))}/>
      <input placeholder="Password" type="password" value={form.password} onChange={e=>setForm(f=>({...f, password:e.target.value}))}/>
      <button type="submit">Sign in</button>
      {msg && <p>{msg}</p>}
    </form>
  )

//const res = await api.login(f)



}
