package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.AddToCartRequest;
import com.shoppingcart.dto.request.UpdateCartItemRequest;
import com.shoppingcart.dto.response.CartResponse;
import com.shoppingcart.entity.Category;
import com.shoppingcart.entity.CartItem;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.InsufficientStockException;
import com.shoppingcart.exception.NothingToUndoException;
import com.shoppingcart.repository.CartItemRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Product mouse;
    private User user;

    @BeforeEach
    void setUp() {
        mouse = Product.builder()
                .id(10L).sku("ELE-001").name("Wireless Mouse").category(Category.ELECTRONICS)
                .price(new BigDecimal("699.00")).stock(50).active(true).build();
        user = User.builder().id(USER_ID).name("Test User").build();
    }

    private CartItem cartItemOf(int quantity) {
        return CartItem.builder().id(100L).user(user).product(mouse).quantity(quantity).addedAt(Instant.now()).build();
    }

    @Test
    void addItem_newProduct_createsLineWithRequestedQuantity() {
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(2)));

        CartResponse response = cartService.addItem(USER_ID, new AddToCartRequest("ELE-001", 2));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);

        assertThat(response.items()).hasSize(1);
        assertThat(response.subtotal()).isEqualByComparingTo("1398.00");
    }

    @Test
    void addItem_existingProduct_mergesQuantityBySkU() {
        CartItem existing = cartItemOf(2);
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001")).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(5)));

        cartService.addItem(USER_ID, new AddToCartRequest("ELE-001", 3));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(5);
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    void addItem_requestExceedsAvailableStock_throwsInsufficientStock() {
        mouse.setStock(4);
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(USER_ID, new AddToCartRequest("ELE-001", 5)))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void undo_afterFreshAdd_removesTheLineEntirely() {
        // Arrange: perform an add for a brand-new line (previousQuantity == null internally).
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001"))
                .thenReturn(Optional.empty(), Optional.of(cartItemOf(2)));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(2)), List.of());

        cartService.addItem(USER_ID, new AddToCartRequest("ELE-001", 2));
        CartResponse afterUndo = cartService.undo(USER_ID);

        verify(cartItemRepository).delete(any(CartItem.class));
        assertThat(afterUndo.items()).isEmpty();
    }

    @Test
    void undo_afterMergeAdd_revertsQuantityRatherThanDeleting() {
        CartItem existing = cartItemOf(2);
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001"))
                .thenReturn(Optional.of(existing), Optional.of(cartItemOf(5)));
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(5)), List.of(cartItemOf(2)));

        cartService.addItem(USER_ID, new AddToCartRequest("ELE-001", 3));
        cartService.undo(USER_ID);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(2)).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void undo_withNoPriorOperations_throwsNothingToUndo() {
        assertThatThrownBy(() -> cartService.undo(USER_ID)).isInstanceOf(NothingToUndoException.class);
    }

    @Test
    void updateItem_requestExceedsStock_throwsInsufficientStock() {
        mouse.setStock(3);
        when(cartItemRepository.findByUserIdAndProductSku(USER_ID, "ELE-001"))
                .thenReturn(Optional.of(cartItemOf(2)));

        assertThatThrownBy(() -> cartService.updateItem(USER_ID, "ELE-001", new UpdateCartItemRequest(10)))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void clearCart_thenUndo_restoresAllPreviousLines() {
        CartItem line = cartItemOf(4);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(line), List.of(), List.of(cartItemOf(4)));
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);

        cartService.clearCart(USER_ID);
        verify(cartItemRepository).deleteByUserId(USER_ID);

        CartResponse restored = cartService.undo(USER_ID);
        assertThat(restored.items()).hasSize(1);
        assertThat(restored.items().get(0).quantity()).isEqualTo(4);
    }
}
