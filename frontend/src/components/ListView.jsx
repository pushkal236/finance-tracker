import { useEffect, useState } from 'react'
import { Box, Button, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material'

export default function ListView() {
  const [from, setFrom] = useState(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0,10))
  const [to,   setTo]   = useState(() => new Date().toISOString().slice(0,10))
  const [rows, setRows] = useState([])

  async function load() {
    const res = await fetch(`/api/transactions?from=${from}&to=${to}`)
    const data = await res.json()
    setRows(data)
  }
  useEffect(()=>{ load() },[])

  return (
    <Paper elevation={2} sx={{ p: 3 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>Transactions</Typography>
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} sm={6} md={3}>
          <TextField label="From" type="date" value={from} onChange={e=>setFrom(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <TextField label="To" type="date" value={to} onChange={e=>setTo(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
        </Grid>
        <Grid item xs={12} sm={6} md={3} alignSelf="center">
          <Button variant="outlined" onClick={load}>Load</Button>
        </Grid>
      </Grid>
      <TableContainer component={Box}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell>Type</TableCell>
              <TableCell align="right">Amount</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Note</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r, i) => (
              <TableRow key={i} hover>
                <TableCell>{r.date}</TableCell>
                <TableCell>{r.type}</TableCell>
                <TableCell align="right">{r.amount}</TableCell>
                <TableCell>{r.category?.name ?? r.category}</TableCell>
                <TableCell>{r.note}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  )
}
