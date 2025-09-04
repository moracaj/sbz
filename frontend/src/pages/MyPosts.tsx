import { useEffect, useMemo, useState } from 'react'

type PostDto = {
  id: number
  text: string
  createdAt: string
  hashtags?: string[]
  likeCount: number
}

const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

function readLoggedInName(): string | null {
  const t = localStorage.getItem('token')
  if (!t) return null
  const parts = t.split('.')
  if (parts.length < 2) return null
  try {
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.name || payload.email || payload.preferred_username || payload.sub || null
  } catch {
    return null
  }
}

export default function MyPosts() {
  const [data, setData] = useState<PostDto[] | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const userName = readLoggedInName()

  // --- form state ---
  const [text, setText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formMsg, setFormMsg] = useState<string | null>(null)

  const previewTags = useMemo(() => {
    const tags = new Set<string>()
    const re = /#([a-zA-Z0-9_]{1,30})/g
    let m
    while ((m = re.exec(text)) !== null) tags.add(m[1].toLowerCase())
    return Array.from(tags)
  }, [text])

  useEffect(() => {
    const t = localStorage.getItem('token')
    if (!t) { setErr('Nema tokena. Prijavi se ponovo.'); return; }

    fetch(`${BASE}/api/me/posts`, {
      headers: { Authorization: `Bearer ${t}` }
    })
      .then(async r => {
        if (!r.ok) throw new Error(await r.text())
        return r.json()
      })
      .then((arr: PostDto[]) => setData(arr.map(p => ({ ...p, hashtags: p.hashtags ?? [] }))))
      .catch(e => setErr(e.message || 'Greška'))
  }, [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setFormMsg(null); setErr(null)
    const t = localStorage.getItem('token')
    if (!t) { setFormMsg('Nema tokena. Prijavi se.'); return }
    const trimmed = text.trim()
    if (!trimmed) { setFormMsg('Unesi tekst objave.'); return }

    try {
      setSubmitting(true)
      const res = await fetch(`${BASE}/api/me/posts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
        body: JSON.stringify({ text: trimmed })
      })
      if (!res.ok) throw new Error(await res.text())
      const created: PostDto = await res.json()
      const withDefaults = { ...created, hashtags: created.hashtags ?? [] }
      setData(prev => prev ? [withDefaults, ...prev] : [withDefaults])
      setText('')
      setFormMsg('Objava sačuvana ✨')
    } catch (e: any) {
      setFormMsg(e.message || 'Neuspešno čuvanje objave.')
    } finally {
      setSubmitting(false)
    }
  }

  const go = (path: string) => { window.location.href = path }

  return (
    <div style={{ display:'grid', gap:16 }}>
      {/* HEADER */}
      <header style={{
        display:'flex', alignItems:'center', justifyContent:'space-between',
        padding:'10px 12px', border:'1px solid #ddd', borderRadius:8
      }}>
        <div style={{ fontWeight:600 }}>SBZ • Moj profil</div>
        <div style={{ opacity:.85 }}>{userName ? `Ulogovan: ${userName}` : 'Nisi prijavljen'}</div>
      </header>

      {/* ACTION BAR */}
      <div style={{ display:'flex', gap:8 }}>
        <button onClick={() => go('/feed')} style={{ padding:'8px 12px', borderRadius:6, border:'1px solid #ccc', cursor:'pointer' }}>
          Pregled tuđih objava
        </button>
        <button onClick={() => go('/Friends')} style={{ padding:'8px 12px', borderRadius:6, border:'1px solid #ccc', cursor:'pointer' }}>
          Prijatelji
        </button>
      </div>

      {/* MAIN: lista levo, forma desno */}
      <div style={{ display:'grid', gap:16, gridTemplateColumns:'2fr 1fr' }}>
        {/* LEVO: lista objava */}
        <div style={{ display:'grid', gap:12 }}>
          <h2 style={{ marginTop:0 }}>Moje objave</h2>
          {err && <div style={{color:'crimson'}}>{err}</div>}
          {!data && !err && <div>Učitavanje…</div>}
          {data?.length === 0 && <div>Još uvek nemaš objava.</div>}
          {data?.map(p => (
            <div key={p.id} style={{border:'1px solid #ddd', padding:12, borderRadius:8}}>
              <div style={{fontSize:12, opacity:.7}}>
                {new Date(p.createdAt).toLocaleString()} · ID {p.id}
              </div>
              <div style={{margin:'6px 0', whiteSpace:'pre-wrap'}}>{p.text}</div>
              <div style={{fontSize:12, opacity:.8}}>
                👍 {p.likeCount} {p.hashtags && p.hashtags.length ? ' • #' + p.hashtags.join(' #') : ''}
              </div>
            </div>
          ))}
        </div>

        {/* DESNO: forma */}
        <div style={{ border:'1px solid #ddd', borderRadius:8, padding:12, height:'fit-content', position:'sticky', top:0 }}>
          <h3 style={{ marginTop:0 }}>Nova objava</h3>
          <form onSubmit={handleSubmit} style={{ display:'grid', gap:8 }}>
            <textarea
              value={text}
              onChange={e => setText(e.target.value)}
              placeholder="Napiši nešto… koristi #tag u tekstu (npr. #java #spring)"
              rows={6}
              style={{ width:'100%', resize:'vertical', padding:8 }}
              disabled={submitting}
            />
            <div style={{ fontSize:12, opacity:.8 }}>
              {previewTags.length > 0 && <>Biće sačuvani tagovi: #{previewTags.join(' #')}</>}
            </div>
            <div style={{ display:'flex', gap:8, alignItems:'center' }}>
              <button
                type="submit"
                disabled={submitting || !text.trim()}
                style={{ padding:'8px 12px', borderRadius:6, border:'1px solid #ccc', cursor:'pointer' }}
              >
                {submitting ? 'Čuvam…' : 'Objavi'}
              </button>
              {formMsg && <span style={{ fontSize:12, opacity:.9 }}>{formMsg}</span>}
            </div>
          </form>
          <div style={{ marginTop:10, fontSize:12, opacity:.7 }}>
            Tagovi se prepoznaju automatski iz teksta (npr. „Učim #Java i #SpringBoot“).
          </div>
        </div>
      </div>
    </div>
  )
}
