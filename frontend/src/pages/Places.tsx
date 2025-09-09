import React, { useEffect, useState } from 'react';
import { listPlaces, getPlace, createPlace, ratePlace, PlaceDto, PlaceDetailsDto, isAdminFromToken } from '../api';

export default function Places() {
  const [items, setItems] = useState<PlaceDto[]>([]);
  const [sel, setSel] = useState<PlaceDetailsDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string>('');
  const [isAdmin, setIsAdmin] = useState<boolean>(false);

  // forms
  const [fName, setFName] = useState('');
  const [fCountry, setFCountry] = useState('');
  const [fCity, setFCity] = useState('');
  const [fDesc, setFDesc] = useState('');
  const [fTags, setFTags] = useState(''); // comma-separated

  const [rScore, setRScore] = useState<number>(5);
  const [rComment, setRComment] = useState('');
  const [rTags, setRTags] = useState('');

  useEffect(() => {
    (async () => {
      try {
        setIsAdmin(isAdminFromToken());
        const data = await listPlaces();
        setItems(data);
      } catch (e: any) {
        setMsg(String(e?.message || e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const openDetails = async (id: number) => {
    try {
      const d = await getPlace(id);
      setSel(d);
    } catch (e: any) {
      setMsg(String(e?.message || e));
    }
  };

  const onCreatePlace = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        name: fName.trim(),
        country: fCountry.trim(),
        city: fCity.trim(),
        description: fDesc,
        hashtags: fTags.split(',').map(s => s.trim()).filter(Boolean).map(s => s.startsWith('#') ? s.slice(1) : s)
      };
      const p = await createPlace(payload);
      setItems(prev => [p, ...prev]);
      setFName(''); setFCountry(''); setFCity(''); setFDesc(''); setFTags('');
      setMsg('Mesto je dodato.');
    } catch (e: any) {
      setMsg(String(e?.message || e));
    }
  };

  const onRate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sel) return;
    try {
      const payload = {
        score: rScore,
        comment: rComment,
        hashtags: rTags.split(',').map(s => s.trim()).filter(Boolean).map(s => s.startsWith('#') ? s.slice(1) : s)
      };
      await ratePlace(sel.id, payload);
      const fresh = await getPlace(sel.id);
      setSel(fresh);
      setRComment(''); setRTags('');
      setMsg('Ocena zabeležena.');
    } catch (e: any) {
      setMsg(String(e?.message || e));
    }
  };

  if (loading) return <div className="p-4">Učitavanje…</div>;

  return (
    <div style={{display:'grid', gap:16, maxWidth:900}}>
      <h2>Mesta</h2>

      {/* ADMIN: dodavanje mesta */}
      {isAdmin && (
        <form onSubmit={onCreatePlace} style={{border:'1px solid #ddd', padding:12, borderRadius:8, display:'grid', gap:8}}>
          <h3>➕ Dodaj mesto (admin)</h3>
          <input placeholder="Naziv" value={fName} onChange={e=>setFName(e.target.value)} required />
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:8}}>
            <input placeholder="Država" value={fCountry} onChange={e=>setFCountry(e.target.value)} required />
            <input placeholder="Grad" value={fCity} onChange={e=>setFCity(e.target.value)} required />
          </div>
          <textarea placeholder="Opis" value={fDesc} onChange={e=>setFDesc(e.target.value)} rows={3} />
          <input placeholder="Hashtagovi (npr: food,ns,museum)" value={fTags} onChange={e=>setFTags(e.target.value)} />
          <button type="submit">Sačuvaj mesto</button>
        </form>
      )}

      {/* lista mesta */}
      <section>
        {items.length === 0 ? <div>Još nema unetih mesta.</div> : (
          <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
            {items.map(p => (
              <li key={p.id} style={{border:'1px solid #ddd', padding:12, borderRadius:8, cursor:'pointer'}} onClick={()=>openDetails(p.id)}>
                <div style={{fontWeight:600}}>{p.name} — {p.city}, {p.country}</div>
                <div style={{fontSize:12, opacity:.8}}>⭐ {p.avgScore.toFixed(1)} ({p.ratingsCount}) · {p.hashtags.map(h => '#'+h).join(' ')}</div>
                <div style={{marginTop:6, whiteSpace:'pre-wrap'}}>{p.description}</div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* detalji + ocene + (admin) forma za ocenu */}
      {sel && (
        <section style={{border:'1px solid #ccc', padding:12, borderRadius:8}}>
          <h3>{sel.name} — {sel.city}, {sel.country}</h3>
          <div style={{fontSize:12, opacity:.8, marginBottom:6}}>⭐ {sel.avgScore.toFixed(1)} ({sel.ratingsCount}) · {sel.hashtags.map(h => '#'+h).join(' ')}</div>
          <p style={{whiteSpace:'pre-wrap'}}>{sel.description}</p>

          <h4 style={{marginTop:12}}>Ocene</h4>
          {sel.ratings.length === 0 ? <div>Još nema ocena.</div> : (
            <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
              {sel.ratings.map(r => (
                <li key={r.id} style={{border:'1px solid #eee', padding:10, borderRadius:8}}>
                  <div><b>{r.authorName}</b> · ⭐ {r.score} · {new Date(r.createdAt).toLocaleString()}</div>
                  <div style={{fontSize:12, opacity:.8}}>{r.hashtags.map(h => '#'+h).join(' ')}</div>
                  <div style={{whiteSpace:'pre-wrap', marginTop:6}}>{r.comment}</div>
                </li>
              ))}
            </ul>
          )}

          {isAdmin && (
            <form onSubmit={onRate} style={{marginTop:12, display:'grid', gap:8}}>
              <h4>➕ Dodaj ocenu (admin)</h4>
              <select value={rScore} onChange={e=>setRScore(parseInt(e.target.value))}>
                {[1,2,3,4,5].map(n => <option key={n} value={n}>{n}</option>)}
              </select>
              <textarea placeholder="Komentar" value={rComment} onChange={e=>setRComment(e.target.value)} rows={3}/>
              <input placeholder="Hashtagovi ocene (npr: view,service)" value={rTags} onChange={e=>setRTags(e.target.value)} />
              <button type="submit">Sačuvaj ocenu</button>
            </form>
          )}
        </section>
      )}

      {msg && <div style={{fontSize:12, opacity:.85}}>{msg}</div>}
    </div>
  );
}
