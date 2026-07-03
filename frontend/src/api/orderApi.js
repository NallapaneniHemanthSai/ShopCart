import client from "./client";

export const orderApi = {
  checkout: (payload) => client.post("/orders/checkout", payload).then((r) => r.data),
  history: () => client.get("/orders").then((r) => r.data),
  getOne: (invoiceNumber) => client.get(`/orders/${invoiceNumber}`).then((r) => r.data),
  cancel: (invoiceNumber) => client.post(`/orders/${invoiceNumber}/cancel`).then((r) => r.data),
  exportUrl: (invoiceNumber, format) =>
    `${client.defaults.baseURL}/orders/${invoiceNumber}/export?format=${format}`,
  downloadExport: (invoiceNumber, format) =>
    client
      .get(`/orders/${invoiceNumber}/export`, { params: { format }, responseType: "blob" })
      .then((r) => r.data),
};
