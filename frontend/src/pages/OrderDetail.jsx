import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { orderApi } from "../api/orderApi";
import { useNotify } from "../context/NotificationContext";
import { extractErrorMessage } from "../api/client";
import InvoiceView from "../components/InvoiceView";
import LoadingSpinner from "../components/LoadingSpinner";

export default function OrderDetail() {
  const { invoiceNumber } = useParams();
  const notify = useNotify();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    orderApi
      .getOne(invoiceNumber)
      .then(setOrder)
      .catch((err) => notify(extractErrorMessage(err), "error"))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [invoiceNumber]);

  if (loading) return <LoadingSpinner label="Loading invoice..." />;
  if (!order) return null;

  return (
    <div className="max-w-3xl mx-auto">
      <Link to="/orders" className="text-sm text-brand-600 hover:underline">
        ← Back to order history
      </Link>
      <div className="mt-4">
        <InvoiceView order={order} />
      </div>
    </div>
  );
}
