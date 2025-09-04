import { useEffect, useState, KeyboardEvent } from 'react'
import { addFriend, getMyFriends, searchUsers } from '../api'

type U = { id:number; firstName:string; lastName:string; email:string; friend:boolean }

export default function Friends(){
  const [q, setQ] = useState('')
  const [results, setResults] = useState<U[]>([])
  const [friends, setFriends] = useState<U[]>([])
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState<string>()

  useEffect(() => {
    getMyFriends().then(setFriends).catch(e=>setMsg(e.message))
  }, [])

  async function doSearch(){
    const query = q.trim()
    if(!query){ setResults([]); return }
    try{
      setLoading(true)
      const res = await searchUsers(query)
      setResults(res)
      setMsg(undefined)
    }catch(e:any){
      setMsg(e.message || 'Greška pri pretrazi')
    }finally{
      setLoading(false)
    }
  }

  function onKeyDown(e: KeyboardEvent<HTMLInputElement>){
    if(e.key === 'Enter'){ doSearch() }
  }

  async function onAdd(u:U){
    try{
      const r = await addFriend(u.id)
      setMsg(r.message)
      setResults(prev => prev.map(x => x.id===u.id ? {...x, friend:true} : x))
      const fresh = await getMyFriends()
      setFriends(fresh)
    }catch(e:any){ setMsg(e.message) }
  }

  return (
    <div style={{display:'grid', gap:16, maxWidth:720}}>
      <h2>Friends</h2>

      <div style={{display:'flex', gap:8}}>
        <input
          placeholder="Pretraži po imenu, prezimenu ili emailu…"
          value={q}
          onChange={e=>setQ(e.target.value)}
          onKeyDown={onKeyDown}
          style={{flex:1, padding:8}}
        />
        <button onClick={doSearch} disabled={loading || !q.trim()} style={{padding:'8px 12px'}}>
          {loading ? 'Tražim…' : 'Search'}
        </button>
      </div>

      {results.length>0 && (
        <div>
          <h3>Rezultati pretrage</h3>
          <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
            {results.map(u=>(
              <li key={u.id} style={{display:'flex', justifyContent:'space-between', alignItems:'center', border:'1px solid #ddd', padding:8, borderRadius:8}}>
                <div>
                  <div style={{fontWeight:600}}>{u.firstName} {u.lastName}</div>
                  <div style={{fontSize:12, opacity:.8}}>{u.email}</div>
                </div>
                <button
                  disabled={u.friend}
                  onClick={()=>onAdd(u)}
                  style={{padding:'6px 10px'}}
                >
                  {u.friend ? 'Već prijatelj' : 'Dodaj prijatelja'}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div>
        <h3>Moji prijatelji</h3>
        {friends.length===0 ? <div>Nema prijatelja još.</div> : (
          <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
            {friends.map(u=>(
              <li key={u.id} style={{border:'1px solid #ddd', padding:8, borderRadius:8}}>
                <div style={{fontWeight:600}}>{u.firstName} {u.lastName}</div>
                <div style={{fontSize:12, opacity:.8}}>{u.email}</div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {msg && <div style={{fontSize:12, opacity:.8}}>{msg}</div>}
    </div>
  )
}
