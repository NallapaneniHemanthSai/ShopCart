import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { productApi } from "../api/productApi";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { formatCategory, formatCurrency } from "../utils/format";
import LoadingSpinner from "../components/LoadingSpinner";

export default function ProductDetail() {
  const { sku } = useParams();
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const notify = useNotify();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    productApi
      .getBySku(sku)
      .then((data) => {
        if (!cancelled) setProduct(data);
      })
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => !cancelled && setLoading(false));

    if (isAuthenticated) {
      productApi.recordView(sku).catch(() => {});
    }
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sku]);

  async function handleAddToCart() {
    if (!isAuthenticated) {
      notify("Please log in to add items to your cart", "error");
      return;
    }
    try {
      await addItem(sku, quantity);
      notify(`Added ${quantity} × ${product.name} to cart`, "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  if (loading) return <LoadingSpinner label="Loading product..." />;
  if (!product) return null;

  const outOfStock = product.stock <= 0;

  return (
    <div>
      <Link to="/" className="text-sm text-brand-600 hover:underline">
        ← Back to catalog
      </Link>

      <div className="mt-4 bg-white rounded-xl border border-slate-200 shadow-sm p-8 max-w-2xl">
        <span className="text-xs font-semibold text-brand-600 bg-brand-50 px-2 py-1 rounded">
          {formatCategory(product.category)}
        </span>
        <h1 className="text-2xl font-bold text-slate-800 mt-3">{product.name}</h1>
        <p className="text-sm text-slate-400 mt-1">SKU: {product.sku}</p>
        <p className="text-slate-600 mt-4">{product.description}</p>

        <div className="mt-6 flex items-center gap-4">
          <span className="text-3xl font-bold text-slate-900">{formatCurrency(product.price)}</span>
          <span className={"text-sm font-medium " + (outOfStock ? "text-red-500" : "text-emerald-600")}>
            {outOfStock ? "Out of stock" : `${product.stock} in stock`}
          </span>
        </div>

        {!outOfStock && (
          <div className="mt-6 flex items-center gap-3">
            <label className="text-sm font-medium text-slate-700">Quantity</label>
            <input
              type="number"
              min={1}
              max={product.stock}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              className="w-20 border border-slate-300 rounded-lg px-3 py-2"
            />
            <button
              onClick={handleAddToCart}
              className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-6 py-2 rounded-lg transition-colors"
            >
              Add to Cart
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
