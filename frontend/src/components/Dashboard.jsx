import { useEffect, useMemo, useState } from 'react'
import { Card, CardContent, Grid, Paper, TextField, Typography, Button } from '@mui/material'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

export default function Dashboard() {
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

  const chartData = useMemo(() => {
    if (!report?.amountByCategory) return []
    return Object.entries(report.amountByCategory).map(([name, value]) => ({ name, value: Number(value) }))
  }, [report])

  return (
    <Paper elevation={2} sx={{ p: 3 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>Dashboard</Typography>
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item>
          <TextField label="Year" type="number" value={year} onChange={e=>setYear(Number(e.target.value))} inputProps={{ min: 2000, max: 2999, style: { width: 100 } }} />
        </Grid>
        <Grid item>
          <TextField label="Month" type="number" value={month} onChange={e=>setMonth(Number(e.target.value))} inputProps={{ min: 1, max: 12, style: { width: 80 } }} />
        </Grid>
        <Grid item alignSelf="center">
          <Button variant="outlined" onClick={load}>Refresh</Button>
        </Grid>
      </Grid>

      {report && (
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={12} sm={4}>
            <Card variant="outlined"><CardContent>
              <Typography color="text.secondary" gutterBottom>Income</Typography>
              <Typography variant="h5">{report.income}</Typography>
            </CardContent></Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card variant="outlined"><CardContent>
              <Typography color="text.secondary" gutterBottom>Expense</Typography>
              <Typography variant="h5">{report.expense}</Typography>
            </CardContent></Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card variant="outlined"><CardContent>
              <Typography color="text.secondary" gutterBottom>Net</Typography>
              <Typography variant="h5">{report.net}</Typography>
            </CardContent></Card>
          </Grid>
        </Grid>
      )}

      <Typography variant="subtitle1" sx={{ mb: 1 }}>Expenses by category</Typography>
      <div style={{ width: '100%', height: 320 }}>
        <ResponsiveContainer>
          <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" hide={chartData.length > 8 ? true : false} />
            <YAxis />
            <Tooltip />
            <Bar dataKey="value" fill="#1976d2" radius={[4,4,0,0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </Paper>
  )
}
