import { Link } from "react-router-dom";
import { formatCategory, formatCurrency } from "../utils/format";

export default function ProductCard({ product, onAddToCart, onToggleWishlist, isWishlisted }) {
  const outOfStock = product.stock <= 0;

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex flex-col overflow-hidden">
      <Link to={`/products/${product.sku}`} className="p-4 flex-1 flex flex-col">
        <div className="flex items-start justify-between">
          <span className="text-xs font-semibold text-brand-600 bg-brand-50 px-2 py-1 rounded">
            {formatCategory(product.category)}
          </span>
          {onToggleWishlist && (
            <button
              onClick={(e) => {
                e.preventDefault();
                onToggleWishlist(product.sku);
              }}
              className={"text-xl leading-none " + (isWishlisted ? "text-red-500" : "text-slate-300 hover:text-red-400")}
              aria-label="Toggle wishlist"
            >
              ♥
            </button>
          )}
        </div>
        <h3 className="mt-2 font-semibold text-slate-800 line-clamp-2">{product.name}</h3>
        <p className="text-xs text-slate-400 mt-1">SKU: {product.sku}</p>
        <p className="text-sm text-slate-500 mt-2 line-clamp-2 flex-1">{product.description}</p>
        <div className="mt-3 flex items-center justify-between">
          <span className="text-lg font-bold text-slate-900">{formatCurrency(product.price)}</span>
          <span className={"text-xs font-medium " + (outOfStock ? "text-red-500" : "text-emerald-600")}>
            {outOfStock ? "Out of stock" : `${product.stock} in stock`}
          </span>
        </div>
      </Link>
      {onAddToCart && (
        <button
          disabled={outOfStock}
          onClick={() => onAddToCart(product.sku)}
          className="m-4 mt-0 bg-brand-600 disabled:bg-slate-200 disabled:text-slate-400 text-white text-sm font-medium py-2 rounded-lg hover:bg-brand-700 transition-colors"
        >
          {outOfStock ? "Unavailable" : "Add to Cart"}
        </button>
      )}
    </div>
  );
}
