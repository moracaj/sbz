export const api = {
  detectBadUsers: async () => {
    const res = await fetch('http://localhost:8080/api/admin/detect-bad-users', { method: 'POST' })
    return res.json()
  }
}


export async function register(data: {
  firstName: string; lastName: string; email: string; password: string; homePlaceId?: number;
}) {
  const r = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(data)
  });
  if(!r.ok){ throw new Error(await r.text()); }
  return r.json();
}

export async function login(data: {email:string; password:string}) {
  const r = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(data)
  });
  if(!r.ok){ throw new Error(await r.text()); }
  return r.json();
}



const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

function authHeaders() {
  const t = localStorage.getItem('token');
  return t ? { 'Authorization': `Bearer ${t}` } : {};
}

export async function searchUsers(q: string) {
  const r = await fetch(`${BASE}/api/users/search?q=${encodeURIComponent(q)}`, {
    headers: { ...authHeaders() }
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json() as Promise<Array<{id:number, firstName:string, lastName:string, email:string, friend:boolean}>>;
}

export async function addFriend(userId: number) {
  const r = await fetch(`${BASE}/api/users/${userId}/friends`, {
    method: 'POST',
    headers: { 'Content-Type':'application/json', ...authHeaders() }
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json() as Promise<{message:string}>;
}

export async function getMyFriends() {
  const r = await fetch(`${BASE}/api/users/me/friends`, { headers: { ...authHeaders() }});
  if (!r.ok) throw new Error(await r.text());
  return r.json() as Promise<Array<{id:number, firstName:string, lastName:string, email:string, friend:boolean}>>;
}
