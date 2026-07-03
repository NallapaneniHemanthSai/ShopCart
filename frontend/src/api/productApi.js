import client from "./client";

export const productApi = {
  list: () => client.get("/products").then((r) => r.data),
  search: (params) => client.get("/products/search", { params }).then((r) => r.data),
  getBySku: (sku) => client.get(`/products/${sku}`).then((r) => r.data),
  recordView: (sku) => client.post(`/products/${sku}/view`).then((r) => r.data),
  recentlyViewed: () => client.get("/products/recently-viewed").then((r) => r.data),
};
