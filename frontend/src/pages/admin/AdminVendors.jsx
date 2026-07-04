import { useEffect, useState } from "react";
import { adminApi } from "../../api/adminApi";
import { useNotify } from "../../context/NotificationContext";
import { extractErrorMessage } from "../../api/client";
import LoadingSpinner from "../../components/LoadingSpinner";

const emptyForm = { name: "", email: "", phone: "", rating: "4.5", verified: true };

export default function AdminVendors() {
  const notify = useNotify();
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    adminApi
      .listVendors()
      .then(setVendors)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  function startCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setShowForm(true);
  }

  function startEdit(vendor) {
    setForm({
      name: vendor.name,
      email: vendor.email || "",
      phone: vendor.phone || "",
      rating: String(vendor.rating),
      verified: vendor.verified,
    });
    setEditingId(vendor.id);
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    const payload = {
      name: form.name,
      email: form.email,
      phone: form.phone,
      rating: Number(form.rating),
      verified: form.verified,
    };
    try {
      if (editingId) {
        await adminApi.updateVendor(editingId, payload);
        notify("Vendor updated", "success");
      } else {
        await adminApi.createVendor(payload);
        notify("Vendor added", "success");
      }
      setShowForm(false);
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <LoadingSpinner label="Loading vendors..." />;

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-slate-500">{vendors.length} vendors on the marketplace</p>
        <button
          onClick={startCreate}
          className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium px-4 py-2 rounded-lg"
        >
          + Add Vendor
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 mb-6 grid grid-cols-2 gap-4"
        >
          <h3 className="col-span-2 font-semibold text-slate-800">
            {editingId ? "Edit Vendor" : "New Vendor"}
          </h3>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Vendor Name</label>
            <input
              required
              value={form.name}
              onChange={update("name")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
            <input
              type="email"
              value={form.email}
              onChange={update("email")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Phone</label>
            <input
              value={form.phone}
              onChange={update("phone")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Rating (0-5)</label>
            <input
              required
              type="number"
              step="0.1"
              min="0"
              max="5"
              value={form.rating}
              onChange={update("rating")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="verified"
              checked={form.verified}
              onChange={(e) => setForm({ ...form, verified: e.target.checked })}
            />
            <label htmlFor="verified" className="text-sm text-slate-700">
              Verified Seller
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
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Contact</th>
              <th className="text-center px-4 py-3">Rating</th>
              <th className="text-center px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {vendors.map((v) => (
              <tr key={v.id}>
                <td className="px-4 py-3 font-medium text-slate-800">{v.name}</td>
                <td className="px-4 py-3 text-slate-500">
                  <div>{v.email}</div>
                  <div className="text-xs">{v.phone}</div>
                </td>
                <td className="px-4 py-3 text-center">
                  <span className="text-amber-500">★</span> {v.rating}
                </td>
                <td className="px-4 py-3 text-center">
                  <span
                    className={
                      "text-xs font-medium px-2 py-1 rounded " +
                      (v.verified ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500")
                    }
                  >
                    {v.verified ? "Verified" : "Unverified"}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <button onClick={() => startEdit(v)} className="text-brand-600 hover:underline text-xs font-medium">
                    Edit
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
