import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { productApi } from "../api/productApi";
import { reviewApi } from "../api/reviewApi";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { formatCategory, formatCurrency, formatDate } from "../utils/format";
import LoadingSpinner from "../components/LoadingSpinner";
import StarRating from "../components/StarRating";

export default function ProductDetail() {
  const { sku } = useParams();
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const notify = useNotify();

  const [product, setProduct] = useState(null);
  const [otherSellers, setOtherSellers] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: "" });
  const [submittingReview, setSubmittingReview] = useState(false);

  function loadReviews() {
    reviewApi.list(sku).then(setReviews).catch(() => {});
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    productApi
      .getBySku(sku)
      .then(async (data) => {
        if (cancelled) return;
        setProduct(data);
        try {
          const sameName = await productApi.search({ q: data.name });
          setOtherSellers(sameName.filter((p) => p.sku !== sku && p.name === data.name));
        } catch {
          // non-critical
        }
      })
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => !cancelled && setLoading(false));

    loadReviews();

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

  async function handleSubmitReview(e) {
    e.preventDefault();
    if (!isAuthenticated) {
      notify("Please log in to leave a review", "error");
      return;
    }
    setSubmittingReview(true);
    try {
      await reviewApi.add(sku, reviewForm);
      notify("Review submitted", "success");
      setReviewForm({ rating: 5, comment: "" });
      loadReviews();
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSubmittingReview(false);
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

      <div className="mt-4 grid grid-cols-1 lg:grid-cols-3 gap-6 max-w-5xl">
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm p-8">
          <span className="text-xs font-semibold text-brand-600 bg-brand-50 px-2 py-1 rounded">
            {formatCategory(product.category)}
          </span>
          <h1 className="text-2xl font-bold text-slate-800 mt-3">{product.name}</h1>
          <p className="text-sm text-slate-400 mt-1">SKU: {product.sku}</p>
          <div className="mt-2">
            <StarRating rating={product.averageRating} count={product.reviewCount} />
          </div>
          <p className="text-slate-600 mt-4">{product.description}</p>

          <div className="mt-6 flex items-center gap-4">
            <span className="text-3xl font-bold text-slate-900">{formatCurrency(product.price)}</span>
            <span className={"text-sm font-medium " + (outOfStock ? "text-red-500" : "text-emerald-600")}>
              {outOfStock ? "Out of stock" : `${product.stock} in stock`}
            </span>
          </div>

          <div className="mt-3 flex items-center gap-2 text-sm text-slate-500">
            <span>Sold by</span>
            <span className="font-semibold text-slate-800">{product.vendorName}</span>
            {product.vendorVerified && (
              <span className="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">✓ Verified Seller</span>
            )}
            <span className="text-amber-500">{"★".repeat(Math.round(product.vendorRating || 0))}</span>
            <span>{product.vendorRating}</span>
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

        {otherSellers.length > 0 && (
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 h-fit">
            <h3 className="font-semibold text-slate-800 mb-3">Other Sellers</h3>
            <div className="space-y-3">
              {[...otherSellers].sort((a, b) => a.price - b.price).map((seller) => (
                <Link
                  key={seller.sku}
                  to={`/products/${seller.sku}`}
                  className="block border border-slate-100 rounded-lg p-3 hover:border-brand-300 hover:bg-brand-50/40 transition-colors"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-sm font-medium text-slate-800">{seller.vendorName}</p>
                      {seller.vendorVerified && <p className="text-xs text-brand-600">✓ Verified</p>}
                    </div>
                    <p className="font-bold text-slate-900">{formatCurrency(seller.price)}</p>
                  </div>
                  <p className="text-xs text-slate-400 mt-1">
                    {seller.stock > 0 ? `${seller.stock} in stock` : "Out of stock"}
                  </p>
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="mt-6 max-w-5xl bg-white rounded-xl border border-slate-200 shadow-sm p-8">
        <h3 className="text-lg font-bold text-slate-800 mb-4">Customer Reviews</h3>

        <form onSubmit={handleSubmitReview} className="mb-6 border-b border-slate-100 pb-6">
          <p className="text-sm font-medium text-slate-700 mb-2">Leave a review (verified purchase required)</p>
          <div className="flex items-center gap-2 mb-3">
            {[1, 2, 3, 4, 5].map((n) => (
              <button
                type="button"
                key={n}
                onClick={() => setReviewForm({ ...reviewForm, rating: n })}
                className={"text-2xl " + (n <= reviewForm.rating ? "text-amber-500" : "text-slate-300")}
              >
                ★
              </button>
            ))}
          </div>
          <textarea
            value={reviewForm.comment}
            onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
            placeholder="Share your experience with this product..."
            rows={2}
            maxLength={500}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          <button
            type="submit"
            disabled={submittingReview}
            className="mt-3 bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white text-sm font-medium px-5 py-2 rounded-lg"
          >
            {submittingReview ? "Submitting..." : "Submit Review"}
          </button>
        </form>

        {reviews.length === 0 ? (
          <p className="text-sm text-slate-400">No reviews yet. Be the first to review this product.</p>
        ) : (
          <div className="space-y-4">
            {reviews.map((r) => (
              <div key={r.id} className="border-b border-slate-50 pb-4 last:border-0">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-slate-800">{r.reviewerName}</span>
                    {r.verifiedPurchase && (
                      <span className="text-[10px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                        Verified Purchase
                      </span>
                    )}
                  </div>
                  <span className="text-xs text-slate-400">{formatDate(r.createdAt)}</span>
                </div>
                <div className="text-amber-500 text-sm mt-1">{"★".repeat(r.rating)}{"☆".repeat(5 - r.rating)}</div>
                {r.comment && <p className="text-sm text-slate-600 mt-1">{r.comment}</p>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
