import { useEffect, useState } from "react";
import { adminApi } from "../../api/adminApi";
import { useNotify } from "../../context/NotificationContext";
import { extractErrorMessage } from "../../api/client";
import { formatCurrency } from "../../utils/format";
import LoadingSpinner from "../../components/LoadingSpinner";

const emptyForm = { code: "", discountType: "PERCENTAGE", value: "", minCartValue: "", active: true };

export default function AdminCoupons() {
  const notify = useNotify();
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingCode, setEditingCode] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    adminApi
      .listCoupons()
      .then(setCoupons)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  function startCreate() {
    setForm(emptyForm);
    setEditingCode(null);
    setShowForm(true);
  }

  function startEdit(coupon) {
    setForm({
      code: coupon.code,
      discountType: coupon.discountType,
      value: coupon.value,
      minCartValue: coupon.minCartValue,
      active: coupon.active,
    });
    setEditingCode(coupon.code);
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    const payload = {
      code: form.code,
      discountType: form.discountType,
      value: Number(form.value),
      minCartValue: Number(form.minCartValue),
      active: form.active,
    };
    try {
      if (editingCode) {
        await adminApi.updateCoupon(editingCode, payload);
        notify("Coupon updated", "success");
      } else {
        await adminApi.createCoupon(payload);
        notify("Coupon created", "success");
      }
      setShowForm(false);
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(code) {
    try {
      await adminApi.deleteCoupon(code);
      notify("Coupon deleted", "success");
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  if (loading) return <LoadingSpinner label="Loading coupons..." />;

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-slate-500">{coupons.length} coupons</p>
        <button
          onClick={startCreate}
          className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium px-4 py-2 rounded-lg"
        >
          + Add Coupon
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 mb-6 grid grid-cols-2 gap-4"
        >
          <h3 className="col-span-2 font-semibold text-slate-800">
            {editingCode ? `Edit ${editingCode}` : "New Coupon"}
          </h3>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Code</label>
            <input
              required
              disabled={!!editingCode}
              value={form.code}
              onChange={update("code")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 uppercase disabled:bg-slate-100"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Discount Type</label>
            <select
              value={form.discountType}
              onChange={update("discountType")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            >
              <option value="PERCENTAGE">Percentage (%)</option>
              <option value="FLAT">Flat Amount (₹)</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Value {form.discountType === "PERCENTAGE" ? "(%)" : "(₹)"}
            </label>
            <input
              required
              type="number"
              step="0.01"
              min="0.01"
              value={form.value}
              onChange={update("value")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Minimum Cart Value (₹)</label>
            <input
              required
              type="number"
              step="0.01"
              min="0"
              value={form.minCartValue}
              onChange={update("minCartValue")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="active"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            <label htmlFor="active" className="text-sm text-slate-700">
              Active
            </label>
          </div>
          <div className="col-span-2 flex gap-2">
            <button
              type="submit"
              disabled={submitting}
              className="bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white font-medium px-5 py-2 rounded-lg"
            >
              {submitting ? "Saving..." : "Save"}
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium px-5 py-2 rounded-lg"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="overflow-x-auto bg-white rounded-xl border border-slate-200 shadow-sm">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
            <tr>
              <th className="text-left px-4 py-3">Code</th>
              <th className="text-left px-4 py-3">Type</th>
              <th className="text-right px-4 py-3">Value</th>
              <th className="text-right px-4 py-3">Min Cart</th>
              <th className="text-center px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {coupons.map((c) => (
              <tr key={c.code}>
                <td className="px-4 py-3 font-mono text-xs font-semibold text-slate-700">{c.code}</td>
                <td className="px-4 py-3 text-slate-500">{c.discountType}</td>
                <td className="px-4 py-3 text-right">
                  {c.discountType === "PERCENTAGE" ? `${c.value}%` : formatCurrency(c.value)}
                </td>
                <td className="px-4 py-3 text-right">{formatCurrency(c.minCartValue)}</td>
                <td className="px-4 py-3 text-center">
                  <span
                    className={
                      "text-xs font-medium px-2 py-1 rounded " +
                      (c.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500")
                    }
                  >
                    {c.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="px-4 py-3 text-right space-x-3">
                  <button onClick={() => startEdit(c)} className="text-brand-600 hover:underline text-xs font-medium">
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(c.code)}
                    className="text-red-500 hover:underline text-xs font-medium"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
