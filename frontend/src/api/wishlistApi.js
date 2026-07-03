import client from "./client";

export const wishlistApi = {
  list: () => client.get("/wishlist").then((r) => r.data),
  add: (sku) => client.post(`/wishlist/${sku}`).then((r) => r.data),
  remove: (sku) => client.delete(`/wishlist/${sku}`).then((r) => r.data),
};
