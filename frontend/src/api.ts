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

