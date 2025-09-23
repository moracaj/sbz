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
