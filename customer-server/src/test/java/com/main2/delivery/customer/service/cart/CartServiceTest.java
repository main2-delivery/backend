package com.main2.delivery.customer.service.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.customer.dto.cart.CartAddRequest;
import com.main2.delivery.customer.exception.CartErrorCode;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;
import com.main2.delivery.customer.repository.store.MenuRepository;
import com.main2.delivery.customer.repository.store.StoreRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // 테스트마다 안 쓰는 가짜 설정이 있어도 경고 안 냄
class CartServiceTest {

    @Mock CartRedisRepository cartRepository;
    @Mock MenuRepository menuRepository;
    @Mock StoreRepository storeRepository;
    @InjectMocks CartService cartService;

    // 가짜 가게 + 메뉴 만들기
    private MenuEntity menu(Long menuId, Long storeId, boolean soldOut) {
        StoreEntity store = mock(StoreEntity.class);
        given(store.getId()).willReturn(storeId);
        given(store.getStatus()).willReturn("OPEN");

        MenuEntity menu = mock(MenuEntity.class);
        given(menu.getId()).willReturn(menuId);
        given(menu.getStore()).willReturn(store);
        given(menu.getPrice()).willReturn(10000);
        given(menu.isSoldOut()).willReturn(soldOut);
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
        return menu;
    }

    private CartAddRequest request(Long menuId, int quantity, boolean clearExisting) {
        CartAddRequest req = new CartAddRequest();
        req.setMenuId(menuId);
        req.setQuantity(quantity);
        req.setClearExisting(clearExisting);
        return req;
    }

    @Test
    @DisplayName("다른 가게 메뉴를 확인 없이 담으면 CART_STORE_MISMATCH")
    void storeMismatch() {
        menu(10L, 2L, false);
        given(cartRepository.findStoreId(1L)).willReturn(1L);   // 장바구니엔 1번 가게

        assertThatThrownBy(() -> cartService.addItem(1L, request(10L, 1, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(CartErrorCode.CART_STORE_MISMATCH);
        verify(cartRepository, never()).addQuantity(anyLong(), anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("clearExisting=true면 비우고 담는다")
    void clearAndAdd() {
        menu(10L, 2L, false);
        given(cartRepository.findStoreId(1L)).willReturn(1L);

        cartService.addItem(1L, request(10L, 1, true));

        InOrder order = inOrder(cartRepository);
        order.verify(cartRepository).clear(1L);
        order.verify(cartRepository).addQuantity(1L, 2L, "10", 1);
    }

    @Test
    @DisplayName("품절 메뉴는 담을 수 없다")
    void soldOut() {
        menu(10L, 1L, true);

        assertThatThrownBy(() -> cartService.addItem(1L, request(10L, 1, false)))
                .extracting("errorCode").isEqualTo(CartErrorCode.CART_MENU_UNAVAILABLE);
    }

    @Test
    @DisplayName("메뉴당 99개를 넘기면 CART_QUANTITY_EXCEEDED")
    void quantityExceeded() {
        menu(10L, 1L, false);
        given(cartRepository.findStoreId(1L)).willReturn(1L);
        given(cartRepository.findQuantity(1L, "10")).willReturn(98);

        assertThatThrownBy(() -> cartService.addItem(1L, request(10L, 2, false)))
                .extracting("errorCode").isEqualTo(CartErrorCode.CART_QUANTITY_EXCEEDED);
    }

    @Test
    @DisplayName("빈 장바구니 조회는 빈 응답")
    void emptyCart() {
        given(cartRepository.findStoreId(1L)).willReturn(null);

        assertThat(cartService.getCart(1L).getItems()).isEmpty();
    }
}