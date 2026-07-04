import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import { NotificationProvider } from "./context/NotificationContext";
import { ProtectedRoute, AdminRoute } from "./routes/ProtectedRoute";
import Layout from "./components/Layout";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Catalog from "./pages/Catalog";
import ProductDetail from "./pages/ProductDetail";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import OrderHistory from "./pages/OrderHistory";
import OrderDetail from "./pages/OrderDetail";
import Wishlist from "./pages/Wishlist";

import AdminLayout from "./pages/admin/AdminLayout";
import AdminProducts from "./pages/admin/AdminProducts";
import AdminVendors from "./pages/admin/AdminVendors";
import AdminCoupons from "./pages/admin/AdminCoupons";
import AdminAnalytics from "./pages/admin/AdminAnalytics";
import AdminGst from "./pages/admin/AdminGst";

export default function App() {
  return (
    <BrowserRouter>
      <NotificationProvider>
        <AuthProvider>
          <CartProvider>
            <Routes>
              <Route element={<Layout />}>
                <Route index element={<Catalog />} />
                <Route path="products/:sku" element={<ProductDetail />} />
                <Route path="login" element={<Login />} />
                <Route path="register" element={<Register />} />

                <Route element={<ProtectedRoute />}>
                  <Route path="cart" element={<Cart />} />
                  <Route path="checkout" element={<Checkout />} />
                  <Route path="orders" element={<OrderHistory />} />
                  <Route path="orders/:invoiceNumber" element={<OrderDetail />} />
                  <Route path="wishlist" element={<Wishlist />} />
                </Route>

                <Route path="admin" element={<AdminRoute />}>
                  <Route element={<AdminLayout />}>
                    <Route index element={<AdminProducts />} />
                    <Route path="vendors" element={<AdminVendors />} />
                    <Route path="coupons" element={<AdminCoupons />} />
                    <Route path="analytics" element={<AdminAnalytics />} />
                    <Route path="gst" element={<AdminGst />} />
                  </Route>
                </Route>
              </Route>
            </Routes>
          </CartProvider>
        </AuthProvider>
      </NotificationProvider>
    </BrowserRouter>
  );
}
