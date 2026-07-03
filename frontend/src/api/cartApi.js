import client from "./client";

export const cartApi = {
  get: (sortBy) => client.get("/cart", { params: sortBy ? { sortBy } : {} }).then((r) => r.data),
  addItem: (sku, quantity) => client.post("/cart/items", { sku, quantity }).then((r) => r.data),
  updateItem: (sku, quantity) => client.put(`/cart/items/${sku}`, { quantity }).then((r) => r.data),
  removeItem: (sku) => client.delete(`/cart/items/${sku}`).then((r) => r.data),
  clear: () => client.delete("/cart").then((r) => r.data),
  undo: () => client.post("/cart/undo").then((r) => r.data),
};
