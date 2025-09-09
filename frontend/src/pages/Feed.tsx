import { useEffect, useState } from 'react'
import { getFeed } from '../api'
import { likePost, reportPost } from '../api'


type Post = {
  id:number; 
  authorId:number; 
  authorName:string;
  text:string; 
  createdAt:string; 
  //likedBy:string[]; 
  likesCount:number;
  hashtags:string[];
   reported?: boolean;
}

export default function Feed(){
  const [friends, setFriends] = useState<Post[]>([])
  const [reco, setReco] = useState<Post[]>([])
  const [msg, setMsg] = useState<string>()

  const updateFriends = (pp:Post) =>
  setFriends(prev => prev.map(x => x.id===pp.id ? pp : x));
const updateReco = (pp:Post) =>
  setReco(prev => prev.map(x => x.id===pp.id ? pp : x));


  useEffect(()=>{
    getFeed()
      .then(r => { setFriends(r.friends || []); setReco(r.recommended || []) })
      .catch(e => setMsg(e.message || 'Greška'))
  },[])

  // const Card = ({p}:{p:Post}) => (
  //   <li key={p.id} style={{border:'1px solid #ddd', padding:12, borderRadius:8}}>
  //     <div style={{fontWeight:600}}>{p.authorName}</div>
  //     <div style={{whiteSpace:'pre-wrap', margin:'6px 0'}}>{p.text}</div>
  //     {/* <div style={{fontSize:12, opacity:.7}}>
  //       ❤️ {p.likesCount} · {new Date(p.createdAt).toLocaleString()} · {p.hashtags?.map(h=>'#'+h).join(' ')}
  //     </div> */}
    

  //   <div style={{display:'flex', gap:8, alignItems:'center', flexWrap:'wrap'}}>
  //     <button
  //       onClick={async () => {
  //         try {
  //           const x = await likePost(p.id);
  //           p.likesCount = x.likesCount;
  //         } catch(e){ console.error(e); }
  //       }}
  //     >❤️ Like ({p.likesCount ?? 0})</button>

  //     <button
  //       disabled={p.reported}
  //       title={p.reported ? 'Već prijavljeno' : 'Prijavi sadržaj'}
  //       onClick={async () => {
  //         try {
  //           const x = await reportPost(p.id);
  //           p.reported = true;
  //         } catch(e){ console.error(e); }
  //       }}
  //     >🚩 Report</button>

  //     <span style={{fontSize:12, opacity:.7}}>
  //       {new Date(p.createdAt).toLocaleString()} · {p.hashtags?.map(h=>'#'+h).join(' ')}
  //     </span>
  //   </div>





  //   </li>
  // )



const Card = ({p, onChange}:{p:Post; onChange:(pp:Post)=>void}) => (
  <li key={p.id} style={{border:'1px solid #ddd', padding:12, borderRadius:8}}>
    <div style={{fontWeight:600}}>{p.authorName}</div>
    <div style={{whiteSpace:'pre-wrap', margin:'6px 0'}}>{p.text}</div>

    <div style={{display:'flex', gap:10, alignItems:'center', flexWrap:'wrap'}}>
      <button
        onClick={async () => {
          try {
            const x = await likePost(p.id);
            onChange({...p, likesCount: x.likesCount});
          } catch (e) { console.error(e); }
        }}
      >
        ❤️ Like ({p.likesCount ?? 0})
      </button>

      <button
        disabled={!!p.reported}
        title={p.reported ? 'Već prijavljeno' : 'Prijavi sadržaj'}
        onClick={async () => {
          try {
            const x = await reportPost(p.id);
            onChange({...p, reported: true});
          } catch (e) { console.error(e); }
        }}
      >
        🚩 Report
      </button>

      <span style={{fontSize:12, opacity:.7}}>
        {new Date(p.createdAt).toLocaleString()} · {p.hashtags?.map(h=>'#'+h).join(' ')}
      </span>
    </div>
  </li>
);







  return (
    <div style={{display:'grid', gap:16, maxWidth:720}}>
      <h2>Feed</h2>

      <section>
        <h3>Objave prijatelja (24h)</h3>
        {friends.length === 0 ? <div>Nema novih objava prijatelja.</div> :
          // <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
          //   {friends.map(p => <Card key={p.id} p={p}/>)}
          // </ul>
          <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
  {friends.map(p => <Card key={p.id} p={p} onChange={updateFriends} />)}
</ul>
        }
      </section>

      <section>
        <h3>Preporučene objave</h3>
        {reco.length === 0 ? <div>Još nema preporuka.</div> :
          // <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
          //   {reco.map(p => <Card key={p.id} p={p}/>)}
          // </ul>
          <ul style={{listStyle:'none', padding:0, display:'grid', gap:8}}>
  {reco.map(p => <Card key={p.id} p={p} onChange={updateReco} />)}
</ul>
        }
      </section>

      {msg && <div style={{fontSize:12, opacity:.8}}>{msg}</div>}
    </div>
  )
}
