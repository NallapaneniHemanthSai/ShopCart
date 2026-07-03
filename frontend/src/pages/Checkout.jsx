import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useNotify } from "../context/NotificationContext";
import { couponApi } from "../api/couponApi";
import { orderApi } from "../api/orderApi";
import { extractErrorMessage } from "../api/client";
import { formatCurrency } from "../utils/format";
import { PAYMENT_METHODS } from "../utils/constants";
import EmptyState from "../components/EmptyState";

export default function Checkout() {
  const { user } = useAuth();
  const { cart, refresh } = useCart();
  const notify = useNotify();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    shippingName: user?.name || "",
    shippingPhone: "",
    shippingAddress: "",
    paymentMethod: "UPI",
  });
  const [couponCode, setCouponCode] = useState("");
  const [couponResult, setCouponResult] = useState(null);
  const [validatingCoupon, setValidatingCoupon] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleApplyCoupon() {
    if (!couponCode.trim()) return;
    setValidatingCoupon(true);
    try {
      const result = await couponApi.validate(couponCode.trim().toUpperCase(), cart.subtotal);
      setCouponResult(result);
      notify(result.valid ? "Coupon applied" : result.message, result.valid ? "success" : "error");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setValidatingCoupon(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      const order = await orderApi.checkout({
        ...form,
        couponCode: couponResult?.valid ? couponCode.trim().toUpperCase() : undefined,
      });
      notify("Order placed successfully!", "success");
      await refresh();
      navigate(`/orders/${order.invoiceNumber}`);
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSubmitting(false);
    }
  }

  if (cart.items.length === 0) {
    return <EmptyState icon="🛒" title="Your cart is empty" subtitle="Add items to your cart before checking out" />;
  }

  const estimatedDiscount = couponResult?.valid ? couponResult.discountAmount : 0;

  return (
    <div className="max-w-3xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">
      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Shipping Details</h2>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Full Name</label>
          <input
            required
            value={form.shippingName}
            onChange={update("shippingName")}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Phone</label>
          <input
            required
            value={form.shippingPhone}
            onChange={update("shippingPhone")}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Delivery Address</label>
          <textarea
            required
            rows={3}
            value={form.shippingAddress}
            onChange={update("shippingAddress")}
            className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Payment Method</label>
          <div className="grid grid-cols-3 gap-2">
            {PAYMENT_METHODS.map((method) => (
              <button
                type="button"
                key={method}
                onClick={() => setForm({ ...form, paymentMethod: method })}
                className={
                  "py-2 rounded-lg text-sm font-medium border " +
                  (form.paymentMethod === method
                    ? "bg-brand-600 text-white border-brand-600"
                    : "bg-white text-slate-600 border-slate-300 hover:bg-slate-50")
                }
              >
                {method}
              </button>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full bg-emerald-600 hover:bg-emerald-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-lg transition-colors"
        >
          {submitting ? "Placing order..." : `Place Order · ${formatCurrency(cart.subtotal - estimatedDiscount)}`}
        </button>
      </form>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 h-fit">
        <h2 className="text-lg font-bold text-slate-800 mb-4">Order Summary</h2>
        <div className="space-y-2 mb-4">
          {cart.items.map((item) => (
            <div key={item.sku} className="flex justify-between text-sm">
              <span className="text-slate-600">
                {item.name} × {item.quantity}
              </span>
              <span className="text-slate-800 font-medium">{formatCurrency(item.lineTotal)}</span>
            </div>
          ))}
        </div>

        <div className="border-t border-slate-100 pt-3 mb-4">
          <label className="block text-sm font-medium text-slate-700 mb-1">Coupon Code</label>
          <div className="flex gap-2">
            <input
              value={couponCode}
              onChange={(e) => {
                setCouponCode(e.target.value);
                setCouponResult(null);
              }}
              placeholder="e.g. SAVE10"
              className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <button
              type="button"
              onClick={handleApplyCoupon}
              disabled={validatingCoupon}
              className="px-4 py-2 rounded-lg bg-slate-800 text-white text-sm font-medium hover:bg-slate-900 disabled:opacity-60"
            >
              Apply
            </button>
          </div>
          {couponResult && (
            <p className={"text-xs mt-1 " + (couponResult.valid ? "text-emerald-600" : "text-red-500")}>
              {couponResult.valid
                ? `Discount: ${formatCurrency(couponResult.discountAmount)}`
                : couponResult.message}
            </p>
          )}
        </div>

        <div className="flex justify-between text-sm text-slate-600">
          <span>Subtotal</span>
          <span>{formatCurrency(cart.subtotal)}</span>
        </div>
        {estimatedDiscount > 0 && (
          <div className="flex justify-between text-sm text-emerald-600">
            <span>Discount</span>
            <span>−{formatCurrency(estimatedDiscount)}</span>
          </div>
        )}
        <p className="text-xs text-slate-400 mt-2">
          Final total including GST and delivery charge will be shown on your invoice.
        </p>
      </div>
    </div>
  );
}
