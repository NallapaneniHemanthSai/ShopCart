package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.AddToCartRequest;
import com.shoppingcart.dto.request.UpdateCartItemRequest;
import com.shoppingcart.dto.response.CartItemResponse;
import com.shoppingcart.dto.response.CartResponse;
import com.shoppingcart.entity.CartItem;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.CartItemNotFoundException;
import com.shoppingcart.exception.InsufficientStockException;
import com.shoppingcart.exception.NothingToUndoException;
import com.shoppingcart.exception.ProductNotFoundException;
import com.shoppingcart.mapper.CartMapper;
import com.shoppingcart.repository.CartItemRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /** Per-user undo history. A Deque used as a Stack: push on every mutation, pop on undo. */
    private final Map<Long, Deque<CartCommand>> undoStacks = new ConcurrentHashMap<>();

    @Override
    public CartResponse getCart(Long userId, String sortBy) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByAddedAtAsc(userId);
        return buildResponse(items, sortBy);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Product product = productRepository.findBySku(request.sku())
                .orElseThrow(() -> new ProductNotFoundException(request.sku()));

        var existing = cartItemRepository.findByUserIdAndProductSku(userId, request.sku());

        Integer previousQuantity = existing.map(CartItem::getQuantity).orElse(null);
        int newQuantity = (previousQuantity == null ? 0 : previousQuantity) + request.quantity();

        if (newQuantity > product.getStock()) {
            throw new InsufficientStockException(request.sku(), newQuantity, product.getStock());
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            User userRef = userRepository.getReferenceById(userId);
            CartItem item = CartItem.builder()
                    .user(userRef)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cartItemRepository.save(item);
        }

        pushCommand(userId, new CartCommand(CommandType.ADD, request.sku(), previousQuantity, null));
        return getCart(userId, null);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, String sku, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByUserIdAndProductSku(userId, sku)
                .orElseThrow(() -> new CartItemNotFoundException(sku));

        if (request.quantity() > item.getProduct().getStock()) {
            throw new InsufficientStockException(sku, request.quantity(), item.getProduct().getStock());
        }

        int previousQuantity = item.getQuantity();
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        pushCommand(userId, new CartCommand(CommandType.UPDATE, sku, previousQuantity, null));
        return getCart(userId, null);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, String sku) {
        CartItem item = cartItemRepository.findByUserIdAndProductSku(userId, sku)
                .orElseThrow(() -> new CartItemNotFoundException(sku));

        int previousQuantity = item.getQuantity();
        cartItemRepository.delete(item);

        pushCommand(userId, new CartCommand(CommandType.REMOVE, sku, previousQuantity, null));
        return getCart(userId, null);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByAddedAtAsc(userId);
        if (!items.isEmpty()) {
            List<CartSnapshot> snapshot = items.stream()
                    .map(i -> new CartSnapshot(i.getProduct().getSku(), i.getQuantity()))
                    .toList();
            cartItemRepository.deleteByUserId(userId);
            pushCommand(userId, new CartCommand(CommandType.CLEAR, null, null, snapshot));
        }
        return getCart(userId, null);
    }

    @Override
    @Transactional
    public CartResponse undo(Long userId) {
        Deque<CartCommand> stack = undoStacks.get(userId);
        if (stack == null || stack.isEmpty()) {
            throw new NothingToUndoException();
        }
        CartCommand command = stack.pop();

        switch (command.type()) {
            case ADD -> undoAdd(userId, command);
            case UPDATE -> undoUpdate(userId, command);
            case REMOVE -> undoRemove(userId, command);
            case CLEAR -> undoClear(userId, command);
        }

        return getCart(userId, null);
    }

    private void undoAdd(Long userId, CartCommand command) {
        var existing = cartItemRepository.findByUserIdAndProductSku(userId, command.sku());
        if (existing.isEmpty()) {
            return;
        }
        if (command.previousQuantity() == null) {
            cartItemRepository.delete(existing.get());
        } else {
            CartItem item = existing.get();
            item.setQuantity(command.previousQuantity());
            cartItemRepository.save(item);
        }
    }

    private void undoUpdate(Long userId, CartCommand command) {
        cartItemRepository.findByUserIdAndProductSku(userId, command.sku()).ifPresent(item -> {
            item.setQuantity(command.previousQuantity());
            cartItemRepository.save(item);
        });
    }

    private void undoRemove(Long userId, CartCommand command) {
        productRepository.findBySku(command.sku()).ifPresent(product -> {
            User userRef = userRepository.getReferenceById(userId);
            CartItem item = CartItem.builder()
                    .user(userRef)
                    .product(product)
                    .quantity(command.previousQuantity())
                    .build();
            cartItemRepository.save(item);
        });
    }

    private void undoClear(Long userId, CartCommand command) {
        User userRef = userRepository.getReferenceById(userId);
        for (CartSnapshot snapshot : command.clearedItems()) {
            productRepository.findBySku(snapshot.sku()).ifPresent(product -> {
                CartItem item = CartItem.builder()
                        .user(userRef)
                        .product(product)
                        .quantity(snapshot.quantity())
                        .build();
                cartItemRepository.save(item);
            });
        }
    }

    @Override
    public void clearHistory(Long userId) {
        undoStacks.remove(userId);
    }

    private void pushCommand(Long userId, CartCommand command) {
        undoStacks.computeIfAbsent(userId, id -> new ArrayDeque<>()).push(command);
    }

    private CartResponse buildResponse(List<CartItem> items, String sortBy) {
        Comparator<CartItem> comparator = resolveComparator(sortBy);
        List<CartItemResponse> responses = items.stream()
                .sorted(comparator)
                .map(CartMapper::toResponse)
                .toList();

        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subtotal = responses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(responses, responses.size(), totalQuantity, subtotal);
    }

    private Comparator<CartItem> resolveComparator(String sortBy) {
        if (sortBy == null) {
            return (a, b) -> 0; // preserve insertion (added-at) order
        }
        return switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(i -> i.getProduct().getPrice());
            case "quantity" -> Comparator.comparing(CartItem::getQuantity, Comparator.reverseOrder());
            case "category" -> Comparator.comparing(i -> i.getProduct().getCategory().name());
            case "name" -> Comparator.comparing(i -> i.getProduct().getName(), String.CASE_INSENSITIVE_ORDER);
            default -> (a, b) -> 0;
        };
    }

    private enum CommandType { ADD, UPDATE, REMOVE, CLEAR }

    private record CartSnapshot(String sku, int quantity) {
    }

    private record CartCommand(CommandType type, String sku, Integer previousQuantity, List<CartSnapshot> clearedItems) {
    }
}
