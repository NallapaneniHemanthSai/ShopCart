import client from "./client";

export const vendorApi = {
  list: () => client.get("/vendors").then((r) => r.data),
};
