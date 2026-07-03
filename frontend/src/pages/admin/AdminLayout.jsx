import { NavLink, Outlet } from "react-router-dom";

const tabClass = ({ isActive }) =>
  "px-4 py-2 rounded-lg text-sm font-medium " +
  (isActive ? "bg-brand-600 text-white" : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50");

export default function AdminLayout() {
  return (
    <div>
      <h1 className="text-xl font-bold text-slate-800 mb-4">Admin Panel</h1>
      <div className="flex gap-2 mb-6">
        <NavLink to="/admin" end className={tabClass}>
          Products
        </NavLink>
        <NavLink to="/admin/coupons" className={tabClass}>
          Coupons
        </NavLink>
        <NavLink to="/admin/analytics" className={tabClass}>
          Analytics
        </NavLink>
        <NavLink to="/admin/gst" className={tabClass}>
          GST Config
        </NavLink>
      </div>
      <Outlet />
    </div>
  );
}
