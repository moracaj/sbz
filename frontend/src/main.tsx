import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import App from './App'
import Login from './pages/Login'
import Register from './pages/Register'
import Feed from './pages/Feed'
import Ads from './pages/Ads'
import AdminBadUsers from './pages/AdminBadUsers'
import MyPosts from './pages/MyPosts'
import Friends from './pages/Friends'


const router = createBrowserRouter([
  { path: '/', element: <App /> },
  { path: '/login', element: <Login /> },
  { path: '/register', element: <Register /> },
  { path: '/feed', element: <Feed /> },
  { path: '/ads', element: <Ads /> },
  { path: '/admin/bad-users', element: <AdminBadUsers /> },
  { path:'/me/posts', element:<MyPosts /> },
  { path: '/friends', element: <Friends /> },
])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
)
