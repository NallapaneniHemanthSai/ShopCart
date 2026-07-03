import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { cartApi } from "../api/cartApi";
import { useAuth } from "./AuthContext";
import { extractErrorMessage } from "../api/client";

const CartContext = createContext(null);

const EMPTY_CART = { items: [], distinctItemCount: 0, totalQuantity: 0, subtotal: 0 };

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(EMPTY_CART);
  const [loading, setLoading] = useState(false);
  const [sortBy, setSortBy] = useState("");

  const refresh = useCallback(
    async (sort = sortBy) => {
      if (!isAuthenticated) {
        setCart(EMPTY_CART);
        return;
      }
      setLoading(true);
      try {
        const data = await cartApi.get(sort || undefined);
        setCart(data);
      } finally {
        setLoading(false);
      }
    },
    [isAuthenticated, sortBy]
  );

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  async function addItem(sku, quantity = 1) {
    const data = await cartApi.addItem(sku, quantity);
    setCart(data);
    return data;
  }

  async function updateItem(sku, quantity) {
    const data = await cartApi.updateItem(sku, quantity);
    setCart(data);
    return data;
  }

  async function removeItem(sku) {
    const data = await cartApi.removeItem(sku);
    setCart(data);
    return data;
  }

  async function clearCart() {
    const data = await cartApi.clear();
    setCart(data);
    return data;
  }

  async function undo() {
    const data = await cartApi.undo();
    setCart(data);
    return data;
  }

  async function changeSort(nextSort) {
    setSortBy(nextSort);
    await refresh(nextSort);
  }

  const value = {
    cart,
    loading,
    sortBy,
    refresh,
    addItem,
    updateItem,
    removeItem,
    clearCart,
    undo,
    changeSort,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) {
    throw new Error("useCart must be used within CartProvider");
  }
  return ctx;
}

export { extractErrorMessage };
