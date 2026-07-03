import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { wishlistApi } from "../api/wishlistApi";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { formatCategory, formatCurrency } from "../utils/format";
import LoadingSpinner from "../components/LoadingSpinner";
import EmptyState from "../components/EmptyState";

export default function Wishlist() {
  const { addItem } = useCart();
  const notify = useNotify();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    wishlistApi
      .list()
      .then(setItems)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleRemove(sku) {
    try {
      const data = await wishlistApi.remove(sku);
      setItems(data);
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleMoveToCart(sku) {
    try {
      await addItem(sku, 1);
      await handleRemove(sku);
      notify("Moved to cart", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  if (loading) return <LoadingSpinner label="Loading wishlist..." />;

  if (items.length === 0) {
    return (
      <EmptyState
        icon="♥"
        title="Your wishlist is empty"
        subtitle="Tap the heart icon on any product to save it here"
        action={
          <Link to="/" className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-5 py-2 rounded-lg inline-block">
            Browse Products
          </Link>
        }
      />
    );
  }

  return (
    <div>
      <h1 className="text-xl font-bold text-slate-800 mb-4">Your Wishlist</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {items.map((item) => (
          <div key={item.sku} className="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
            <span className="text-xs font-semibold text-brand-600 bg-brand-50 px-2 py-1 rounded">
              {formatCategory(item.category)}
            </span>
            <h3 className="font-semibold text-slate-800 mt-2">{item.name}</h3>
            <p className="text-lg font-bold text-slate-900 mt-1">{formatCurrency(item.price)}</p>
            <p className="text-xs text-slate-400 mb-3">{item.stock > 0 ? `${item.stock} in stock` : "Out of stock"}</p>
            <div className="flex gap-2">
              <button
                disabled={item.stock <= 0}
                onClick={() => handleMoveToCart(item.sku)}
                className="flex-1 bg-brand-600 disabled:bg-slate-200 disabled:text-slate-400 text-white text-sm font-medium py-2 rounded-lg hover:bg-brand-700"
              >
                Move to Cart
              </button>
              <button
                onClick={() => handleRemove(item.sku)}
                className="px-3 py-2 rounded-lg bg-red-50 text-red-600 text-sm hover:bg-red-100"
              >
                Remove
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
