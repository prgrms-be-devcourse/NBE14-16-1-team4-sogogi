import { Routes, Route } from 'react-router-dom'
import ProductsPage from './pages/customer/ProductsPage.jsx'
import OrdersPage from './pages/customer/OrdersPage.jsx'
import AdminProductsPage from './pages/admin/AdminProductsPage.jsx'
import AdminOrdersPage from './pages/admin/AdminOrdersPage.jsx'

export default function App() {
  return (
      <Routes>
        <Route path="/" element={<ProductsPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/admin/products" element={<AdminProductsPage />} />
        <Route path="/admin/orders" element={<AdminOrdersPage />} />
      </Routes>
  )
}