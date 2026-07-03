import client from "./client";

export const couponApi = {
  validate: (code, subtotal) =>
    client.get("/coupons/validate", { params: { code, subtotal } }).then((r) => r.data),
};
