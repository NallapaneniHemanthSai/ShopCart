import { useEffect, useState } from "react";
import { adminApi } from "../../api/adminApi";
import { useNotify } from "../../context/NotificationContext";
import { extractErrorMessage } from "../../api/client";
import { formatCurrency } from "../../utils/format";
import LoadingSpinner from "../../components/LoadingSpinner";

function StatCard({ label, value }) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
      <p className="text-xs text-slate-400 uppercase tracking-wide">{label}</p>
      <p className="text-2xl font-bold text-slate-900 mt-1">{value}</p>
    </div>
  );
}

export default function AdminAnalytics() {
  const notify = useNotify();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi
      .analytics()
      .then(setData)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) return <LoadingSpinner label="Crunching numbers..." />;
  if (!data) return null;

  const maxUnits = Math.max(1, ...data.topSellingProducts.map((p) => p.unitsSold));

  return (
    <div>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <StatCard label="Total Revenue" value={formatCurrency(data.totalRevenue)} />
        <StatCard label="Total Orders" value={data.totalOrders} />
        <StatCard label="Average Order Value" value={formatCurrency(data.averageOrderValue)} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          <h3 className="font-semibold text-slate-800 mb-4">Top Selling Products</h3>
          {data.topSellingProducts.length === 0 ? (
            <p className="text-sm text-slate-400">No sales yet.</p>
          ) : (
            <div className="space-y-3">
              {data.topSellingProducts.map((p) => (
                <div key={p.sku}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="font-medium text-slate-700">{p.name}</span>
                    <span className="text-slate-500">
                      {p.unitsSold} sold · {formatCurrency(p.revenue)}
                    </span>
                  </div>
                  <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-brand-500 rounded-full"
                      style={{ width: `${(p.unitsSold / maxUnits) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          <h3 className="font-semibold text-slate-800 mb-4">Low Stock Alerts</h3>
          {data.lowStockProducts.length === 0 ? (
            <p className="text-sm text-slate-400">All products are well stocked.</p>
          ) : (
            <div className="space-y-2">
              {data.lowStockProducts.map((p) => (
                <div key={p.sku} className="flex justify-between items-center text-sm">
                  <span className="text-slate-700">{p.name}</span>
                  <span className="text-red-500 font-semibold">{p.stock} left</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
