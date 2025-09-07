import { useEffect, useState } from 'react';
import { api, isAdminFromToken, type SuspensionDto } from '../api';

export default function AdminBadUsers() {
  const [rows, setRows] = useState<SuspensionDto[]>([]);
  const [msg, setMsg]   = useState('');
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => { setIsAdmin(isAdminFromToken()); }, []);

  const run = async () => {
    if (!isAdmin) {
      setMsg('403 — nemaš dozvolu (ADMIN). Uloguj se kao admin.');
      return; // ključ: ne šaljemo zahtev => nema 403 u konzoli
    }
    try {
      const data = await api.detectBadUsers();
      setRows(data);
      setMsg('');
    } catch (e:any) {
      setMsg(String(e?.message || e));
    }
  };

  return (
    <div>
      <h2>Loši korisnici (admin)</h2>

      <button
        onClick={run}
        disabled={!isAdmin}
        title={!isAdmin ? 'Samo admin može da pokrene detekciju' : undefined}
        style={{ width:'100%' }}
      >
        Pokreni detekciju
      </button>

      {msg && <div style={{marginTop:8, color:'crimson'}}>{msg}</div>}

      <table style={{marginTop:12, borderCollapse:'collapse', width:'100%'}}>
        <thead><tr><th>User</th><th>Tip</th><th>Od</th><th>Do</th><th>Razlog</th></tr></thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={5} style={{opacity:.8}}>Nema podataka.</td></tr>
          ) : rows.map((r,i)=>(
            <tr key={i}>
              <td>{r.user?.id} — {r.user?.firstName} {r.user?.lastName}</td>
              <td>{r.type}</td>
              <td>{r.startAt}</td>
              <td>{r.endAt ?? '—'}</td>
              <td>{r.reason}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {!isAdmin && (
        <div style={{marginTop:12, fontSize:13}}>
          403 — nemaš dozvolu (ADMIN). Uloguj se kao admin.
        </div>
      )}
    </div>
  );
}
