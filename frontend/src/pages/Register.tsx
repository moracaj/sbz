import { useState } from 'react'
import { register } from '../api'

export default function Register(){
  const [form, setForm] = useState({firstName:'', lastName:'', email:'', password:'', homePlaceId:''})
  const [msg, setMsg] = useState<string|undefined>()

  const submit = async (e:any) => {
    e.preventDefault()
    setMsg(undefined)
    try{
      const payload:any = {...form, homePlaceId: form.homePlaceId ? Number(form.homePlaceId) : undefined}
      const res = await register(payload)
      localStorage.setItem('token', res.token)
      setMsg('Registered! Token saved.')
    }catch(err:any){ setMsg(err.message || 'Error') }
  }

  return (
    <form onSubmit={submit} style={{display:'grid', gap:8, maxWidth:420}}>
      <h2>Register</h2>
      <input placeholder="First name" value={form.firstName} onChange={e=>setForm(f=>({...f, firstName:e.target.value}))}/>
      <input placeholder="Last name" value={form.lastName} onChange={e=>setForm(f=>({...f, lastName:e.target.value}))}/>
      <input placeholder="Email" value={form.email} onChange={e=>setForm(f=>({...f, email:e.target.value}))}/>
      <input placeholder="Password" type="password" value={form.password} onChange={e=>setForm(f=>({...f, password:e.target.value}))}/>
      <input placeholder="Home place ID (optional)" value={form.homePlaceId} onChange={e=>setForm(f=>({...f, homePlaceId:e.target.value}))}/>
      <button type="submit">Create account</button>
      {msg && <p>{msg}</p>}
    </form>
  )
}
