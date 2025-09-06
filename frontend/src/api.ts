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


async function authedGET<T = any>(path: string): Promise<T> {
  const r = await fetch(`${BASE}${path}`, { headers: authHeaders() })
  if (!r.ok) throw new Error(await r.text())
  return r.json()
}

async function authedPOST<T = any>(path: string, body?: any): Promise<T> {
  const r = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: authHeaders(),
    body: body ? JSON.stringify(body) : undefined
  })
  if (!r.ok) throw new Error(await r.text())
  return r.json()
}

async function authedDELETE<T = any>(path: string): Promise<T> {
  const r = await fetch(`${BASE}${path}`, {
    method: 'DELETE',
    headers: authHeaders()
  })
  if (!r.ok) throw new Error(await r.text())
  return r.json()
}

// (opciono) tip za povrat sa user pretragе/lista
type UserSummary = {
  id: number
  firstName: string
  lastName: string
  email: string
  friend: boolean
  blocked?: boolean
}

// --- nove funkcije koje Friends.tsx koristi ---
export async function getBlocked(): Promise<UserSummary[]> {
  return authedGET('/api/users/me/blocked')
}

export async function blockUser(id: number): Promise<{ message: string }> {
  return authedPOST(`/api/users/${id}/block`)
}

export async function unblockUser(id: number): Promise<{ message: string }> {
  return authedDELETE(`/api/users/${id}/block`)
}

export async function getFeed(){
  return authedGET<{friends:any[], recommended:any[]}>('/api/feed')
}

export async function likePost(postId: number){
  return authedPOST<{postId:number; likesCount:number; reported:boolean}>(`/api/posts/${postId}/like`);
}

export async function reportPost(postId: number){
  return authedPOST<{postId:number; likesCount:number; reported:boolean}>(`/api/posts/${postId}/report`);
}
