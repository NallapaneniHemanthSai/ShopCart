import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

const linkClass = ({ isActive }) =>
  "px-3 py-2 rounded-md text-sm font-medium transition-colors " +
  (isActive ? "bg-brand-600 text-white" : "text-slate-600 hover:bg-slate-100");

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="bg-white border-b border-slate-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 flex items-center justify-between h-16">
        <Link to="/" className="flex items-center gap-2 font-bold text-lg text-brand-700">
          <span>🛒</span>
          <span>ShopCart</span>
        </Link>

        <div className="flex items-center gap-1">
          <NavLink to="/" className={linkClass} end>
            Catalog
          </NavLink>
          {isAuthenticated && (
            <>
              <NavLink to="/wishlist" className={linkClass}>
                Wishlist
              </NavLink>
              <NavLink to="/orders" className={linkClass}>
                Orders
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className={linkClass}>
                  Admin
                </NavLink>
              )}
            </>
          )}
        </div>

        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              <Link
                to="/cart"
                className="relative px-3 py-2 rounded-md text-sm font-medium text-slate-600 hover:bg-slate-100"
              >
                Cart
                {cart.totalQuantity > 0 && (
                  <span className="absolute -top-1 -right-1 bg-brand-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                    {cart.totalQuantity}
                  </span>
                )}
              </Link>
              <span className="text-sm text-slate-500 hidden sm:inline">{user.name}</span>
              <button
                onClick={handleLogout}
                className="px-3 py-2 rounded-md text-sm font-medium bg-slate-100 text-slate-700 hover:bg-slate-200"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="px-3 py-2 rounded-md text-sm font-medium text-slate-600 hover:bg-slate-100">
                Login
              </Link>
              <Link
                to="/register"
                className="px-3 py-2 rounded-md text-sm font-medium bg-brand-600 text-white hover:bg-brand-700"
              >
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
