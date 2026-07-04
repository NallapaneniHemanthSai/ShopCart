import client from "./client";

export const userApi = {
  me: () => client.get("/users/me").then((r) => r.data),
};
