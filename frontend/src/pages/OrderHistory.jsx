import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { orderApi } from "../api/orderApi";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import { formatCurrency, formatDate } from "../utils/format";
import LoadingSpinner from "../components/LoadingSpinner";
import EmptyState from "../components/EmptyState";

export default function OrderHistory() {
  const notify = useNotify();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    orderApi
      .history()
      .then(setOrders)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) return <LoadingSpinner label="Loading orders..." />;

  if (orders.length === 0) {
    return (
      <EmptyState
        icon="📦"
        title="No orders yet"
        subtitle="Your purchase history will appear here after checkout"
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
      <h1 className="text-xl font-bold text-slate-800 mb-4">Order History</h1>
      <div className="space-y-3">
        {orders.map((order) => (
          <Link
            key={order.invoiceNumber}
            to={`/orders/${order.invoiceNumber}`}
            className="block bg-white rounded-xl border border-slate-200 shadow-sm p-4 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold text-slate-800">{order.invoiceNumber}</p>
                <p className="text-xs text-slate-400">{formatDate(order.createdAt)}</p>
              </div>
              <div className="text-right">
                <p className="font-bold text-slate-900">{formatCurrency(order.totalAmount)}</p>
                <p className="text-xs text-slate-400">{order.items.length} item(s) · {order.paymentMethod}</p>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
