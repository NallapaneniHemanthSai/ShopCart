import client from "./client";

export const adminApi = {
  listProducts: () => client.get("/admin/products").then((r) => r.data),
  createProduct: (payload) => client.post("/admin/products", payload).then((r) => r.data),
  updateProduct: (sku, payload) => client.put(`/admin/products/${sku}`, payload).then((r) => r.data),
  adjustStock: (sku, delta) => client.patch(`/admin/products/${sku}/stock`, { delta }).then((r) => r.data),
  deactivateProduct: (sku) => client.delete(`/admin/products/${sku}`).then((r) => r.data),
  lowStock: (threshold = 5) =>
    client.get("/admin/products/low-stock", { params: { threshold } }).then((r) => r.data),

  listCoupons: () => client.get("/admin/coupons").then((r) => r.data),
  createCoupon: (payload) => client.post("/admin/coupons", payload).then((r) => r.data),
  updateCoupon: (code, payload) => client.put(`/admin/coupons/${code}`, payload).then((r) => r.data),
  deleteCoupon: (code) => client.delete(`/admin/coupons/${code}`).then((r) => r.data),

  getGst: () => client.get("/admin/gst").then((r) => r.data),
  updateGst: (ratePercent) => client.put("/admin/gst", { ratePercent }).then((r) => r.data),

  listVendors: () => client.get("/admin/vendors").then((r) => r.data),
  createVendor: (payload) => client.post("/admin/vendors", payload).then((r) => r.data),
  updateVendor: (id, payload) => client.put(`/admin/vendors/${id}`, payload).then((r) => r.data),

  analytics: () => client.get("/admin/analytics").then((r) => r.data),
};
