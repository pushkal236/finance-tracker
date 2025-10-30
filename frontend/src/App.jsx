import { useEffect, useMemo, useState } from 'react'

function TabButton({ active, onClick, children }) {
  return (
    <button onClick={onClick} style={{
      padding: '8px 12px', marginRight: 8,
      border: '1px solid #ccc', borderBottom: active ? '2px solid #333' : '1px solid #ccc',
      background: active ? '#f8f8f8' : 'white', cursor: 'pointer'
    }}>{children}</button>
  )
}

export default function App() {
  const [tab, setTab] = useState('add')
  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: 16 }}>
      <h1>Finance Tracker (React + Java)</h1>
      <div style={{ marginBottom: 16 }}>
        <TabButton active={tab==='add'} onClick={() => setTab('add')}>Add</TabButton>
        <TabButton active={tab==='list'} onClick={() => setTab('list')}>List</TabButton>
        <TabButton active={tab==='dash'} onClick={() => setTab('dash')}>Dashboard</TabButton>
      </div>
      {tab==='add' && <AddForm />}
      {tab==='list' && <ListView />}
      {tab==='dash' && <Dashboard />}
    </div>
  )
}

function AddForm() {
  const [date, setDate] = useState(() => new Date().toISOString().slice(0,10))
  const [type, setType] = useState('EXPENSE')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState('')
  const [note, setNote] = useState('')
  const [msg, setMsg] = useState('')

  async function submit(e) {
    e.preventDefault()
    setMsg('')
    try {
      const res = await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ date, amount: Number(amount), type, category, note })
      })
      if (!res.ok) throw new Error('Request failed')
      setMsg('Saved')
      setAmount(''); setCategory(''); setNote('')
    } catch (err) {
      setMsg('Error: ' + err.message)
    }
  }

  return (
    <form onSubmit={submit} style={{ maxWidth: 420 }}>
      <div style={{ marginBottom: 8 }}>
        <label>Date<br/>
          <input type="date" value={date} onChange={e=>setDate(e.target.value)} />
        </label>
      </div>
      <div style={{ marginBottom: 8 }}>
        <label>Type<br/>
          <select value={type} onChange={e=>setType(e.target.value)}>
            <option>INCOME</option>
            <option>EXPENSE</option>
          </select>
        </label>
      </div>
      <div style={{ marginBottom: 8 }}>
        <label>Amount<br/>
          <input type="number" step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} required />
        </label>
      </div>
      <div style={{ marginBottom: 8 }}>
        <label>Category<br/>
          <input value={category} onChange={e=>setCategory(e.target.value)} required />
        </label>
      </div>
      <div style={{ marginBottom: 8 }}>
        <label>Note<br/>
          <input value={note} onChange={e=>setNote(e.target.value)} />
        </label>
      </div>
      <button>Save</button>
      <div style={{ marginTop: 8 }}>{msg}</div>
    </form>
  )
}

function ListView() {
  const [from, setFrom] = useState(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0,10))
  const [to, setTo] = useState(() => new Date().toISOString().slice(0,10))
  const [rows, setRows] = useState([])

  async function load() {
    const res = await fetch(`/api/transactions?from=${from}&to=${to}`)
    const data = await res.json()
    setRows(data)
  }
  useEffect(()=>{ load() },[])

  return (
    <div>
      <div style={{ marginBottom: 12 }}>
        <label>From <input type="date" value={from} onChange={e=>setFrom(e.target.value)} /></label>
        <label style={{ marginLeft: 12 }}>To <input type="date" value={to} onChange={e=>setTo(e.target.value)} /></label>
        <button style={{ marginLeft: 12 }} onClick={load}>Load</button>
      </div>
      <table width="100%" cellPadding="6" style={{ borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th align="left">Date</th>
            <th align="left">Type</th>
            <th align="right">Amount</th>
            <th align="left">Category</th>
            <th align="left">Note</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i} style={{ borderTop: '1px solid #eee' }}>
              <td>{r.date}</td>
              <td>{r.type}</td>
              <td align="right">{r.amount}</td>
              <td>{r.category?.name ?? r.category}</td>
              <td>{r.note}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function Dashboard() {
  const today = useMemo(()=> new Date(), [])
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth()+1)
  const [report, setReport] = useState(null)

  async function load() {
    const res = await fetch(`/api/reports/monthly?year=${year}&month=${month}`)
    const data = await res.json()
    setReport(data)
  }
  useEffect(()=>{ load() },[])

  return (
    <div>
      <div style={{ marginBottom: 12 }}>
        <label>Year <input type="number" value={year} onChange={e=>setYear(Number(e.target.value))} style={{ width: 100 }} /></label>
        <label style={{ marginLeft: 12 }}>Month <input type="number" value={month} onChange={e=>setMonth(Number(e.target.value))} min={1} max={12} style={{ width: 60 }} /></label>
        <button style={{ marginLeft: 12 }} onClick={load}>Refresh</button>
      </div>
      {report && (
        <div>
          <div>Income: {report.income}</div>
          <div>Expense: {report.expense}</div>
          <div>Net: {report.net}</div>
          <h3 style={{ marginTop: 12 }}>Expenses by category</h3>
          <ul>
            {Object.entries(report.amountByCategory || {}).map(([k,v]) => (
              <li key={k}>{k}: {v}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
