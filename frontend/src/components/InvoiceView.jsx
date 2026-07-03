import { formatCategory, formatCurrency, formatDate } from "../utils/format";
import { orderApi } from "../api/orderApi";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";

export default function InvoiceView({ order }) {
  const notify = useNotify();

  async function handleExport(format) {
    try {
      const blob = await orderApi.downloadExport(order.invoiceNumber, format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${order.invoiceNumber}.${format}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    }
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div className="flex items-start justify-between border-b border-slate-100 pb-4 mb-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800">Invoice {order.invoiceNumber}</h2>
          <p className="text-sm text-slate-400">{formatDate(order.createdAt)}</p>
          <span className="inline-block mt-2 text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-1 rounded">
            {order.status}
          </span>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => handleExport("txt")}
            className="text-xs font-medium px-3 py-1.5 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
          >
            Export TXT
          </button>
          <button
            onClick={() => handleExport("csv")}
            className="text-xs font-medium px-3 py-1.5 rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200"
          >
            Export CSV
          </button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 text-sm mb-6">
        <div>
          <p className="text-slate-400">Shipping To</p>
          <p className="font-medium text-slate-800">{order.shippingName}</p>
          <p className="text-slate-600">{order.shippingPhone}</p>
          <p className="text-slate-600">{order.shippingAddress}</p>
        </div>
        <div className="text-right">
          <p className="text-slate-400">Payment Method</p>
          <p className="font-medium text-slate-800">{order.paymentMethod}</p>
        </div>
      </div>

      <table className="w-full text-sm mb-4">
        <thead className="text-xs text-slate-400 uppercase border-b border-slate-100">
          <tr>
            <th className="text-left py-2">Product</th>
            <th className="text-left py-2">Category</th>
            <th className="text-right py-2">Price</th>
            <th className="text-center py-2">Qty</th>
            <th className="text-right py-2">Total</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-50">
          {order.items.map((item) => (
            <tr key={item.sku}>
              <td className="py-2 font-medium text-slate-800">{item.name}</td>
              <td className="py-2 text-slate-500">{formatCategory(item.category)}</td>
              <td className="py-2 text-right text-slate-700">{formatCurrency(item.unitPrice)}</td>
              <td className="py-2 text-center text-slate-700">{item.quantity}</td>
              <td className="py-2 text-right font-medium text-slate-900">{formatCurrency(item.lineTotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="ml-auto max-w-xs space-y-1 text-sm">
        <div className="flex justify-between text-slate-600">
          <span>Subtotal</span>
          <span>{formatCurrency(order.subtotal)}</span>
        </div>
        {order.couponCode && (
          <div className="flex justify-between text-emerald-600">
            <span>Coupon ({order.couponCode})</span>
            <span>−{formatCurrency(order.discountAmount)}</span>
          </div>
        )}
        <div className="flex justify-between text-slate-600">
          <span>GST ({order.gstRate}%)</span>
          <span>{formatCurrency(order.gstAmount)}</span>
        </div>
        <div className="flex justify-between text-slate-600">
          <span>Delivery Charge</span>
          <span>{order.deliveryCharge > 0 ? formatCurrency(order.deliveryCharge) : "FREE"}</span>
        </div>
        <div className="flex justify-between text-base font-bold text-slate-900 border-t border-slate-100 pt-2 mt-2">
          <span>Total</span>
          <span>{formatCurrency(order.totalAmount)}</span>
        </div>
      </div>
    </div>
  );
}
