import { useState } from 'react'
import { Box, Button, Grid, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material'

export default function AddForm() {
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
    <Paper elevation={2} sx={{ p: 3 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>Add Transaction</Typography>
      <Box component="form" onSubmit={submit} noValidate>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={3}>
            <TextField label="Date" type="date" value={date} onChange={e=>setDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField select label="Type" value={type} onChange={e=>setType(e.target.value)} fullWidth>
              <MenuItem value="INCOME">INCOME</MenuItem>
              <MenuItem value="EXPENSE">EXPENSE</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField label="Amount" type="number" inputProps={{ step: '0.01' }} value={amount} onChange={e=>setAmount(e.target.value)} required fullWidth />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField label="Category" value={category} onChange={e=>setCategory(e.target.value)} required fullWidth />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Note" value={note} onChange={e=>setNote(e.target.value)} fullWidth />
          </Grid>
        </Grid>
        <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
          <Button type="submit" variant="contained">Save</Button>
          <Typography variant="body2" color={msg.startsWith('Error') ? 'error' : 'success.main'}>{msg}</Typography>
        </Stack>
      </Box>
    </Paper>
  )
}
