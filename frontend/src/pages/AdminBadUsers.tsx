// import { useEffect, useState } from 'react';
// import { api, isAdminFromToken, type SuspensionDto, listActiveSuspensions } from '../api';

// export default function AdminBadUsers() {
//   const [rows, setRows]   = useState<SuspensionDto[]>([]);
//   const [msg, setMsg]     = useState('');
//   const [isAdmin, setIsAdmin] = useState(false);
//   const [loading, setLoading] = useState(false);

//   // na mount: odredi admina i učitaj aktivne suspenzije (vidljivo svima)
//   useEffect(() => {
//     setIsAdmin(isAdminFromToken());
//     (async () => {
//       try {
//         const data = await listActiveSuspensions();
//         setRows(data);
//       } catch (e:any) {
//         setMsg(String(e?.message || e));
//       }
//     })();
//   }, []);

//   const run = async () => {
//     if (!isAdmin) {
//       setMsg('403 — nemaš dozvolu (ADMIN). Uloguj se kao admin.');
//       return; // ne šaljemo request -> nema 403 u konzoli
//     }
//     setLoading(true);
//     setMsg('');
//     try {
//       const data = await api.detectBadUsers(); // POST /api/detection/run
//       setRows(data);
//       setMsg(`Detekcija završena. Nađeno: ${data.length}`);
//     } catch (e:any) {
//       setMsg(String(e?.message || e));
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div>
//       <h2>Loši korisnici</h2>

//       <button
//         onClick={run}
//         disabled={!isAdmin || loading}
//         title={!isAdmin ? 'Samo admin može da pokrene detekciju' : undefined}
//         style={{ width:'100%' }}
//       >
//         {loading ? 'Radim…' : 'Pokreni detekciju'}
//       </button>

//       {msg && <div style={{marginTop:8, color:'crimson'}}>{msg}</div>}

//       <table style={{marginTop:12, borderCollapse:'collapse', width:'100%'}}>
//         <thead>
//           <tr>
//             <th>User</th><th>Tip</th><th>Od</th><th>Do</th><th>Razlog</th>
//           </tr>
//         </thead>
//         <tbody>
//           {rows.length === 0 ? (
//             <tr><td colSpan={5} style={{opacity:.8}}>Nema podataka.</td></tr>
//           ) : rows.map((r) => (
//             <tr key={r.id}>
//               <td>{r.userName}{r.userId ? ` (#${r.userId})` : ''}</td>
//               <td>{r.type ?? '-'}</td>
//               <td>{r.startAt ? new Date(r.startAt).toLocaleString() : '-'}</td>
//               <td>{r.endAt ? new Date(r.endAt).toLocaleString() : '—'}</td>
//               <td>{r.reason ?? '-'}</td>
//             </tr>
//           ))}
//         </tbody>
//       </table>

//       {!isAdmin && (
//         <div style={{marginTop:12, fontSize:13}}>
//           * Kao običan korisnik vidiš listu, ali ne možeš da pokrećeš detekciju.
//         </div>
//       )}
//     </div>
//   );
// }


import { useEffect, useState } from 'react';
import { api, isAdminFromToken, type SuspensionDto } from '../api';
import './AdminBadUsers.css';

export default function AdminBadUsers() {
  const [rows, setRows] = useState<SuspensionDto[]>([]);
  const [msg, setMsg] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(false);
  const [show, setShow] = useState(false); // <= tabela se ne prikazuje dok ne klikneš

  useEffect(() => {
    setIsAdmin(isAdminFromToken());
    // namerno NE učitavamo ništa na mount
  }, []);

  const formatDT = (iso?: string | null) => (iso ? new Date(iso).toLocaleString() : '—');

  const run = async () => {
    if (!isAdmin) {
      setMsg('403 — samo admin može da pokrene detekciju.');
      setShow(false);
      return;
    }
    setLoading(true);
    setMsg('');
    try {
      const data = await api.detectBadUsers(); // POST /api/detection/run
      setRows(data);
      setShow(true);
      setMsg(`Detekcija završena. Nađeno: ${data.length}`);
    } catch (e: any) {
      setMsg(String(e?.message || e));
      setShow(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="badusers-page">
      <div className="page-head">
        <h1>Loši korisnici</h1>
        <button
          onClick={run}
          disabled={!isAdmin || loading}
          className={`btn ${isAdmin ? '' : 'btn-disabled'}`}
          title={!isAdmin ? 'Samo admin može da pokrene detekciju' : undefined}
        >
          {loading ? 'Radim…' : 'Pokreni detekciju'}
        </button>
      </div>

      {msg && <div className="status">{msg}</div>}

      {show && (
        <div className="table-card">
          <div className="table-wrap">
            <table className="nice-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Tip</th>
                  <th>Od</th>
                  <th>Do</th>
                  <th>Razlog</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="empty">Nema podataka.</td>
                  </tr>
                ) : (
                  rows.map((r) => (
                    <tr key={r.id}>
                      <td className="user">
                        {r.userName}{r.userId ? ` (#${r.userId})` : ''}
                      </td>
                      <td>
                        <span className={`badge ${String(r.type || 'UNKNOWN').toLowerCase()}`}>
                          {r.type ?? '-'}
                        </span>
                      </td>
                      <td>{formatDT(r.startAt)}</td>
                      <td>{formatDT(r.endAt)}</td>
                      <td className="reason">{r.reason ?? '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!isAdmin && (
        <p className="hint">* Kao običan korisnik ne možeš da pokreneš detekciju.</p>
      )}
    </div>
  );
}
