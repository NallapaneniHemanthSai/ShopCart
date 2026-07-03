import { formatCategory, formatCurrency } from "../utils/format";

export default function CartTable({ items, onQuantityChange, onRemove }) {
  return (
    <div className="overflow-x-auto bg-white rounded-xl border border-slate-200 shadow-sm">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
          <tr>
            <th className="text-left px-4 py-3">Product</th>
            <th className="text-left px-4 py-3">Category</th>
            <th className="text-right px-4 py-3">Price</th>
            <th className="text-center px-4 py-3">Qty</th>
            <th className="text-right px-4 py-3">Total</th>
            <th className="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={item.sku}>
              <td className="px-4 py-3">
                <p className="font-medium text-slate-800">{item.name}</p>
                <p className="text-xs text-slate-400">SKU: {item.sku}</p>
              </td>
              <td className="px-4 py-3 text-slate-500">{formatCategory(item.category)}</td>
              <td className="px-4 py-3 text-right text-slate-700">{formatCurrency(item.unitPrice)}</td>
              <td className="px-4 py-3">
                <div className="flex items-center justify-center gap-2">
                  <button
                    onClick={() => onQuantityChange(item.sku, Math.max(1, item.quantity - 1))}
                    className="w-7 h-7 rounded-full border border-slate-300 text-slate-600 hover:bg-slate-100"
                  >
                    −
                  </button>
                  <span className="w-6 text-center">{item.quantity}</span>
                  <button
                    disabled={item.quantity >= item.availableStock}
                    onClick={() => onQuantityChange(item.sku, item.quantity + 1)}
                    className="w-7 h-7 rounded-full border border-slate-300 text-slate-600 hover:bg-slate-100 disabled:opacity-40"
                  >
                    +
                  </button>
                </div>
              </td>
              <td className="px-4 py-3 text-right font-semibold text-slate-900">
                {formatCurrency(item.lineTotal)}
              </td>
              <td className="px-4 py-3 text-right">
                <button
                  onClick={() => onRemove(item.sku)}
                  className="text-red-500 hover:text-red-700 text-xs font-medium"
                >
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
