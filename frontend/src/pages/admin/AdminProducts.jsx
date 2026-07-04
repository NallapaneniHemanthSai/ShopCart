import { useEffect, useState } from "react";
import { adminApi } from "../../api/adminApi";
import { useNotify } from "../../context/NotificationContext";
import { extractErrorMessage } from "../../api/client";
import { CATEGORIES } from "../../utils/constants";
import { formatCategory, formatCurrency } from "../../utils/format";
import LoadingSpinner from "../../components/LoadingSpinner";

const baseForm = {
  sku: "",
  name: "",
  description: "",
  category: CATEGORIES[0],
  vendorId: "",
  price: "",
  stock: "",
  imageUrl: "",
  active: true,
};

export default function AdminProducts() {
  const notify = useNotify();
  const [products, setProducts] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingSku, setEditingSku] = useState(null);
  const [form, setForm] = useState(baseForm);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    Promise.all([adminApi.listProducts(), adminApi.listVendors()])
      .then(([productList, vendorList]) => {
        setProducts(productList);
        setVendors(vendorList);
      })
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  function emptyForm() {
    return { ...baseForm, vendorId: vendors[0]?.id ?? "" };
  }

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  function startCreate() {
    setForm(emptyForm());
    setEditingSku(null);
    setShowForm(true);
  }

  function startEdit(product) {
    setForm({
      sku: product.sku,
      name: product.name,
      description: product.description || "",
      category: product.category,
      vendorId: product.vendorId,
      price: product.price,
      stock: product.stock,
      imageUrl: product.imageUrl || "",
      active: product.active,
    });
    setEditingSku(product.sku);
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (editingSku) {
        await adminApi.updateProduct(editingSku, {
          name: form.name,
          description: form.description,
          category: form.category,
          vendorId: Number(form.vendorId),
          price: Number(form.price),
          stock: Number(form.stock),
          active: form.active,
          imageUrl: form.imageUrl,
        });
        notify("Product updated", "success");
      } else {
        await adminApi.createProduct({
          sku: form.sku,
          name: form.name,
          description: form.description,
          category: form.category,
          vendorId: Number(form.vendorId),
          price: Number(form.price),
          stock: Number(form.stock),
          imageUrl: form.imageUrl,
        });
        notify("Product created", "success");
      }
      setShowForm(false);
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleStockAdjust(sku, delta) {
    try {
      await adminApi.adjustStock(sku, delta);
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleToggleActive(product) {
    try {
      await adminApi.updateProduct(product.sku, {
        name: product.name,
        description: product.description,
        category: product.category,
        vendorId: product.vendorId,
        price: product.price,
        stock: product.stock,
        active: !product.active,
        imageUrl: product.imageUrl,
      });
      notify(product.active ? "Product deactivated" : "Product activated", "success");
      load();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  if (loading) return <LoadingSpinner label="Loading products..." />;

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-slate-500">{products.length} products</p>
        <button
          onClick={startCreate}
          className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium px-4 py-2 rounded-lg"
        >
          + Add Product
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 mb-6 grid grid-cols-2 gap-4"
        >
          <h3 className="col-span-2 font-semibold text-slate-800">
            {editingSku ? `Edit ${editingSku}` : "New Product"}
          </h3>
          {!editingSku && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">SKU</label>
              <input
                required
                value={form.sku}
                onChange={update("sku")}
                className="w-full border border-slate-300 rounded-lg px-3 py-2"
              />
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
            <input
              required
              value={form.name}
              onChange={update("name")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div className="col-span-2">
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea
              value={form.description}
              onChange={update("description")}
              rows={2}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Category</label>
            <select
              value={form.category}
              onChange={update("category")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {formatCategory(c)}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Vendor</label>
            <select
              required
              value={form.vendorId}
              onChange={update("vendorId")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            >
              <option value="" disabled>Select a vendor</option>
              {vendors.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Price (₹)</label>
            <input
              required
              type="number"
              step="0.01"
              min="0.01"
              value={form.price}
              onChange={update("price")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Stock</label>
            <input
              required
              type="number"
              min="0"
              value={form.stock}
              onChange={update("stock")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Image URL (optional)</label>
            <input
              value={form.imageUrl}
              onChange={update("imageUrl")}
              className="w-full border border-slate-300 rounded-lg px-3 py-2"
            />
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
              <th className="text-left px-4 py-3">SKU</th>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Category</th>
              <th className="text-left px-4 py-3">Vendor</th>
              <th className="text-right px-4 py-3">Price</th>
              <th className="text-center px-4 py-3">Stock</th>
              <th className="text-center px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {products.map((p) => (
              <tr key={p.sku} className={p.active ? "" : "opacity-50"}>
                <td className="px-4 py-3 font-mono text-xs text-slate-500">{p.sku}</td>
                <td className="px-4 py-3 font-medium text-slate-800">{p.name}</td>
                <td className="px-4 py-3 text-slate-500">{formatCategory(p.category)}</td>
                <td className="px-4 py-3 text-slate-500">{p.vendorName}</td>
                <td className="px-4 py-3 text-right">{formatCurrency(p.price)}</td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-center gap-2">
                    <button
                      onClick={() => handleStockAdjust(p.sku, -1)}
                      className="w-6 h-6 rounded-full border border-slate-300 hover:bg-slate-100"
                    >
                      −
                    </button>
                    <span className={p.stock <= 5 ? "text-red-500 font-semibold" : ""}>{p.stock}</span>
                    <button
                      onClick={() => handleStockAdjust(p.sku, 1)}
                      className="w-6 h-6 rounded-full border border-slate-300 hover:bg-slate-100"
                    >
                      +
                    </button>
                  </div>
                </td>
                <td className="px-4 py-3 text-center">
                  <button
                    onClick={() => handleToggleActive(p)}
                    className={
                      "text-xs font-medium px-2 py-1 rounded " +
                      (p.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500")
                    }
                  >
                    {p.active ? "Active" : "Inactive"}
                  </button>
                </td>
                <td className="px-4 py-3 text-right">
                  <button onClick={() => startEdit(p)} className="text-brand-600 hover:underline text-xs font-medium">
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
