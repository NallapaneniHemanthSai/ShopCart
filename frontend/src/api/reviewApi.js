import client from "./client";

export const reviewApi = {
  list: (sku) => client.get(`/products/${sku}/reviews`).then((r) => r.data),
  add: (sku, payload) => client.post(`/products/${sku}/reviews`, payload).then((r) => r.data),
};
