import { useState } from 'react'
import { AppBar, Box, Container, CssBaseline, Tab, Tabs, Toolbar, Typography } from '@mui/material'
import AddForm from './components/AddForm.jsx'
import ListView from './components/ListView.jsx'
import Dashboard from './components/Dashboard.jsx'

export default function App() {
  const [tab, setTab] = useState('add')
  return (
    <>
      <CssBaseline />
      <AppBar position="static" color="primary" enableColorOnDark>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Finance Tracker</Typography>
          <Tabs value={tab} onChange={(_,v)=>setTab(v)} textColor="inherit" indicatorColor="secondary">
            <Tab value="add" label="Add" />
            <Tab value="list" label="List" />
            <Tab value="dash" label="Dashboard" />
          </Tabs>
        </Toolbar>
      </AppBar>
      <Box sx={{ bgcolor: 'background.default', minHeight: '100vh' }}>
        <Container sx={{ py: 3 }}>
          {tab==='add' && <AddForm />}
          {tab==='list' && <ListView />}
          {tab==='dash' && <Dashboard />}
        </Container>
      </Box>
    </>
  )
}
