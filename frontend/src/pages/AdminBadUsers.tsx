import { useState } from 'react'
import { api } from '../api'

type Suspension = {
  id:number; user:{ id:number, firstName:string, lastName:string }|any;
  startAt:string; endAt:string; type:string; reason:string
}

export default function AdminBadUsers(){
  const [rows, setRows] = useState<Suspension[]>([])
  const run = async () => setRows(await api.detectBadUsers())
  return (
    <div>
      <h2>Admin – Bad Users</h2>
      <button onClick={run}>Run detection</button>
      <table style={{marginTop:12, borderCollapse:'collapse', width:'100%'}}>
        <thead><tr><th>User</th><th>Type</th><th>From</th><th>To</th><th>Reason</th></tr></thead>
        <tbody>
          {rows.map((r,i)=>(
            <tr key={i}>
              <td>{r.user?.id}</td>
              <td>{r.type}</td>
              <td>{r.startAt}</td>
              <td>{r.endAt}</td>
              <td>{r.reason}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
