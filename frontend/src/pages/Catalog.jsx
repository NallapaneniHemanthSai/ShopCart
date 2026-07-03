import { useEffect, useState } from "react";
import { productApi } from "../api/productApi";
import { wishlistApi } from "../api/wishlistApi";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { CATEGORIES } from "../utils/constants";
import { formatCategory } from "../utils/format";
import ProductCard from "../components/ProductCard";
import LoadingSpinner from "../components/LoadingSpinner";
import EmptyState from "../components/EmptyState";

export default function Catalog() {
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const notify = useNotify();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [sortBy, setSortBy] = useState("");
  const [wishlistSkus, setWishlistSkus] = useState(new Set());
  const [recentlyViewed, setRecentlyViewed] = useState([]);

  async function loadProducts() {
    setLoading(true);
    try {
      const data = await productApi.search({
        q: query || undefined,
        category: category || undefined,
        sortBy: sortBy || undefined,
      });
      setProducts(data);
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setLoading(false);
    }
  }

  async function loadWishlist() {
    if (!isAuthenticated) return;
    try {
      const data = await wishlistApi.list();
      setWishlistSkus(new Set(data.map((w) => w.sku)));
    } catch {
      // non-critical, ignore
    }
  }

  async function loadRecentlyViewed() {
    if (!isAuthenticated) return;
    try {
      const data = await productApi.recentlyViewed();
      setRecentlyViewed(data);
    } catch {
      // non-critical, ignore
    }
  }

  useEffect(() => {
    loadProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category, sortBy]);

  useEffect(() => {
    loadWishlist();
    loadRecentlyViewed();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  function handleSearchSubmit(e) {
    e.preventDefault();
    loadProducts();
  }

  async function handleAddToCart(sku) {
    if (!isAuthenticated) {
      notify("Please log in to add items to your cart", "error");
      return;
    }
    try {
      await addItem(sku, 1);
      notify("Added to cart", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  async function handleToggleWishlist(sku) {
    if (!isAuthenticated) {
      notify("Please log in to use your wishlist", "error");
      return;
    }
    try {
      if (wishlistSkus.has(sku)) {
        await wishlistApi.remove(sku);
        setWishlistSkus((prev) => {
          const next = new Set(prev);
          next.delete(sku);
          return next;
        });
      } else {
        await wishlistApi.add(sku);
        setWishlistSkus((prev) => new Set(prev).add(sku));
      }
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  return (
    <div>
      <div className="bg-white rounded-xl border border-slate-200 p-4 mb-6 shadow-sm">
        <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search products by name or SKU..."
            className="flex-1 border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">All Categories</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {formatCategory(c)}
              </option>
            ))}
          </select>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Sort: Default</option>
            <option value="name">Name (A-Z)</option>
            <option value="price">Price (Low-High)</option>
            <option value="price_desc">Price (High-Low)</option>
            <option value="stock">Stock</option>
            <option value="category">Category</option>
          </select>
          <button
            type="submit"
            className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-5 py-2 rounded-lg transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {recentlyViewed.length > 0 && (
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-2">Recently Viewed</h2>
          <div className="flex gap-3 overflow-x-auto pb-2">
            {recentlyViewed.map((p) => (
              <div key={p.sku} className="min-w-[160px]">
                <ProductCard product={p} />
              </div>
            ))}
          </div>
        </div>
      )}

      {loading ? (
        <LoadingSpinner label="Loading products..." />
      ) : products.length === 0 ? (
        <EmptyState icon="🔍" title="No products found" subtitle="Try a different search term or category" />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {products.map((product) => (
            <ProductCard
              key={product.sku}
              product={product}
              onAddToCart={handleAddToCart}
              onToggleWishlist={handleToggleWishlist}
              isWishlisted={wishlistSkus.has(product.sku)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
