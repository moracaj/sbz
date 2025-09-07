export type SuspensionDto = {
  id: number;
  user: { id:number; firstName:string; lastName:string };
  startAt: string;
  endAt: string | null;
  type: string;
  reason: string;
};

// ispravljena funkcija – koristi isti helper kao i admin “places”
export const api = {
  detectBadUsers: async (): Promise<SuspensionDto[]> =>
    authedPOST('/api/admin/detect-bad-users', {}),  // POST na /api/admin/...
};

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



async function req(method: string, path: string, body?: any) {
  const isForm = body instanceof FormData;
  const headers = new Headers();
  const t = localStorage.getItem('token');
  if (t) headers.set('Authorization', 'Bearer ' + t);
  if (!isForm) headers.set('Content-Type', 'application/json');

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body === undefined ? undefined : (isForm ? body : JSON.stringify(body)),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText}: ${text}`);
  }
  const ct = res.headers.get('content-type') || '';
  return ct.includes('application/json') ? res.json() : res.text();
}




// function authHeaders() {
//   const t = localStorage.getItem('token');
//   return t ? { 'Authorization': `Bearer ${t}` } : {};
// }
function authHeaders(): Record<string, string> {
  const t = localStorage.getItem('token');
  const h: Record<string, string> = { 'Content-Type': 'application/json' };
  if (t) h['Authorization'] = 'Bearer ' + t;
  return h;
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




export async function authedGET<T>(path: string): Promise<T> {
  return req('GET', path);
}
export async function authedPOST<T>(path: string, body?: any): Promise<T> {
  return req('POST', path, body);
}
export async function authedDELETE<T>(path: string): Promise<T> {
  return req('DELETE', path);
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



////////////////////////////////////////

// === Places API ===
export type PlaceDto = {
  id: number;
  name: string;
  country: string;
  city: string;
  description: string;
  hashtags: string[];
  avgScore: number;
  ratingsCount: number;
};

export type RatingDto = {
  id: number;
  authorId: number | null;
  authorName: string;
  score: number;         // 1..5
  comment: string;
  hashtags: string[];
  createdAt: string;     // ISO
};

export type PlaceDetailsDto = PlaceDto & {
  ratings: RatingDto[];
};

export async function listPlaces(): Promise<PlaceDto[]> {
  return authedGET('/api/places');
}

export async function getPlace(id: number): Promise<PlaceDetailsDto> {
  return authedGET(`/api/places/${id}`);
}

export async function createPlace(payload: {
  name: string; country: string; city: string; description?: string; hashtags?: string[];
}): Promise<PlaceDto> {
  return authedPOST('/api/places', payload);
}

export async function ratePlace(placeId: number, payload: {
  score: number; comment?: string; hashtags?: string[];
}): Promise<RatingDto> {
  return authedPOST(`/api/places/${placeId}/ratings`, payload);
}

// Pomoćno: detekcija admina iz JWT (ako jwt sadrži 'admin' claim)
export function isAdminFromToken(): boolean {
  try {
    const t = localStorage.getItem('token');
    if (!t) return false;
    const payload = JSON.parse(atob(t.split('.')[1] || '')) || {};
    return !!payload.admin; // JwtAuthFilter ti već puni authorities po ovom claimu
  } catch { return false; }
}



