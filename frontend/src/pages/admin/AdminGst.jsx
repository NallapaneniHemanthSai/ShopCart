import { useEffect, useState } from "react";
import { adminApi } from "../../api/adminApi";
import { useNotify } from "../../context/NotificationContext";
import { extractErrorMessage } from "../../api/client";
import LoadingSpinner from "../../components/LoadingSpinner";

export default function AdminGst() {
  const notify = useNotify();
  const [rate, setRate] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    adminApi
      .getGst()
      .then((data) => setRate(String(data.ratePercent)))
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    try {
      const data = await adminApi.updateGst(Number(rate));
      setRate(String(data.ratePercent));
      notify("GST rate updated", "success");
    } catch (err) {
      notify(extractErrorMessage(err), "error");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <LoadingSpinner label="Loading GST config..." />;

  return (
    <div className="max-w-sm bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 className="font-semibold text-slate-800 mb-1">Store-wide GST Rate</h3>
      <p className="text-sm text-slate-400 mb-4">Applied to every order's taxable amount at checkout.</p>
      <form onSubmit={handleSubmit} className="flex items-end gap-3">
        <div className="flex-1">
          <label className="block text-sm font-medium text-slate-700 mb-1">Rate (%)</label>
          <input
            required
            type="number"
            step="0.01"
            min="0"
            max="100"
            value={rate}
            onChange={(e) => setRate(e.target.value)}
            className="w-full border border-slate-300 rounded-lg px-3 py-2"
          />
        </div>
        <button
          type="submit"
          disabled={saving}
          className="bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white font-medium px-5 py-2 rounded-lg"
        >
          {saving ? "Saving..." : "Update"}
        </button>
      </form>
    </div>
  );
}
