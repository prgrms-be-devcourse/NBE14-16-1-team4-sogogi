import { Routes, Route } from 'react-router-dom'
import ProductsPage from './pages/customer/ProductsPage'
import OrdersPage from './pages/customer/OrdersPage'
import AdminProductsPage from './pages/admin/AdminProductsPage'
import AdminOrdersPage from './pages/admin/AdminOrdersPage'

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