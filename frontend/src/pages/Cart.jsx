import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { formatCurrency } from "../utils/format";
import CartTable from "../components/CartTable";
import EmptyState from "../components/EmptyState";

export default function Cart() {
  const { cart, updateItem, removeItem, clearCart, undo, sortBy, changeSort } = useCart();
  const notify = useNotify();

  async function handleQuantityChange(sku, quantity) {
    try {
      await updateItem(sku, quantity);
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleRemove(sku) {
    try {
      await removeItem(sku);
      notify("Item removed", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleClear() {
    try {
      await clearCart();
      notify("Cart cleared", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleUndo() {
    try {
      await undo();
      notify("Last action undone", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  if (cart.items.length === 0) {
    return (
      <EmptyState
        icon="🛒"
        title="Your cart is empty"
        subtitle="Browse the catalog and add products to get started"
        action={
          <Link
            to="/"
            className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-5 py-2 rounded-lg inline-block"
          >
            Browse Products
          </Link>
        }
      />
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-bold text-slate-800">Your Cart ({cart.totalQuantity} items)</h1>
        <div className="flex items-center gap-2">
          <select
            value={sortBy}
            onChange={(e) => changeSort(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Sort: Default</option>
            <option value="name">Name</option>
            <option value="price">Price</option>
            <option value="quantity">Quantity</option>
            <option value="category">Category</option>
          </select>
          <button
            onClick={handleUndo}
            className="text-sm font-medium px-3 py-1.5 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
          >
            ↩ Undo
          </button>
          <button
            onClick={handleClear}
            className="text-sm font-medium px-3 py-1.5 rounded-lg bg-red-50 text-red-600 hover:bg-red-100"
          >
            Clear Cart
          </button>
        </div>
      </div>

      <CartTable items={cart.items} onQuantityChange={handleQuantityChange} onRemove={handleRemove} />

      <div className="mt-6 flex justify-end">
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 w-full max-w-sm">
          <div className="flex justify-between text-slate-600 mb-2">
            <span>Subtotal</span>
            <span className="font-semibold text-slate-900">{formatCurrency(cart.subtotal)}</span>
          </div>
          <p className="text-xs text-slate-400 mb-4">GST, discounts and delivery are calculated at checkout.</p>
          <Link
            to="/checkout"
            className="block text-center bg-brand-600 hover:bg-brand-700 text-white font-medium py-2.5 rounded-lg transition-colors"
          >
            Proceed to Checkout
          </Link>
        </div>
      </div>
    </div>
  );
}
