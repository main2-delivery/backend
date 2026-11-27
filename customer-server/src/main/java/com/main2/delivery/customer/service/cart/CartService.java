package com.main2.delivery.customer.service.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.customer.dto.cart.CartAddRequest;
import com.main2.delivery.customer.dto.cart.CartItemResponse;
import com.main2.delivery.customer.dto.cart.CartQuantityRequest;
import com.main2.delivery.customer.dto.cart.CartResponse;
import com.main2.delivery.customer.exception.CartErrorCode;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;
import com.main2.delivery.customer.repository.store.MenuRepository;
import com.main2.delivery.customer.repository.store.StoreRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CartService {
    private static final int MAX_QUANTITY = 99;
    private final CartRedisRepository cartRedisRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    // [1] 장바구니 조회(금액은 현재 DB 가격으로 계산)
    @Transactional (readOnly = true)
    public CartResponse getCart(Long userId){
        Long storeId = cartRedisRepository.findStoreId(userId);
        Map<String, Integer> items = cartRedisRepository.findItems(userId);
        if(storeId == null || items.isEmpty()){
            return new CartResponse();
        }
        
        StoreEntity store = storeRepository.findById(storeId).orElse(null);
        if(store == null){      // 가게가 없어졌으면 장바구니도 비움
            cartRedisRepository.clear(userId);
            return new CartResponse();
        }

        List<CartItemResponse> result = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : items.entrySet()){
            String cartItemId = entry.getKey();
            MenuEntity menu = menuRepository.findById(Long.valueOf(cartItemId)).orElse(null);
            // 삭제된 메뉴는 장바구니에서 정리 (품절/숨김은 남겨두고 주문 불가로 표시)
            if(menu == null || menu.isDeleted()){
                cartRedisRepository.removeItem(userId, cartItemId);
                continue;
            }
            result.add(new CartItemResponse(cartItemId, menu, entry.getValue()));
        }
        if (result.isEmpty()){
            return new CartResponse();
        }
        return new CartResponse(store, result);
    }

    // [2] 담기 (가게당 장바구니 1개, 같은 메뉴는 수량 증가)
    @Transactional(readOnly = true)
    public CartResponse addItem(Long userId, CartAddRequest req){
        if(req.getMenuId() == null){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "메뉴 번호는 필수");
        }
        checkQuantity(req.getQuantity());

        MenuEntity menu = findMenu(req.getMenuId());
        if(!isOrderable(menu)){
            throw new BusinessException(CartErrorCode.CART_MENU_UNAVAILABLE);
        }
        StoreEntity store = menu.getStore();    // 가게는 요청값이 아니라 메뉴에서 꺼냄
        if(!"OPEN".equals(store.getStatus())){
            throw new BusinessException(CartErrorCode.CART_STORE_CLOSED);
        }

        // 다른 가게 메뉴가 담겨있으면: 확이 플래그가 없으면 409, 있으면 비우고 담기
        Long currentStoreId = cartRedisRepository.findStoreId(userId);
        if(currentStoreId != null && !currentStoreId.equals(store.getId())){
            if(!req.isClearExisting()){
                throw new BusinessException(CartErrorCode.CART_STORE_MISMATCH);
            }
            cartRedisRepository.clear(userId);
        }
        
        String cartItemId = String.valueOf(menu.getId());
        int current = cartRedisRepository.findQuantity(userId, cartItemId);
        if(current + req.getQuantity() > MAX_QUANTITY){
            throw new BusinessException(CartErrorCode.CART_QUANTITY_EXCEEDED);
        }

        cartRedisRepository.addQuantity(userId, store.getId(), cartItemId, req.getQuantity());
        return getCart(userId);
    }

    // [3] 수량 변경
    @Transactional (readOnly = true)
    public CartResponse changeQuantity(Long userId, String cartItemId, CartQuantityRequest req){
        checkQuantity(req.getQuantity());
        checkItemExists(userId, cartItemId);
        cartRedisRepository.setQuantity(userId, cartItemId, req.getQuantity());
        return getCart(userId);
    }

    // [4] 항목 삭제 
    @Transactional (readOnly = true)
    public CartResponse removeItem(Long userId, String cartItemId){
        checkItemExists(userId, cartItemId);
        cartRedisRepository.removeItem(userId, cartItemId);
        return getCart(userId);
    }

    // [5] 전체 비우기
    public void clear(Long userId){
        cartRedisRepository.clear(userId);
    }

    // 메뉴 한건 찾기 (없으면 404)
    private MenuEntity findMenu(Long menuId){
        return menuRepository.findById(menuId).orElseThrow(()-> new BusinessException(CartErrorCode.CART_MENU_NOT_FOUND));
    }

    // 담을 수 있는 메뉴인지 (가격있음 + 품절/숨김/삭제 아님)
    private boolean isOrderable(MenuEntity menu){
        return menu.getPrice() != null && !menu.isSoldOut() && !menu.isHidden() && !menu.isDeleted();
    }

    // 수량 범위 확인(1~99가 아니면 400)
    private void checkQuantity(int quantity){
        if(quantity < 1 || quantity > MAX_QUANTITY){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "수량은 1~99개");
        }
    }

    // 장바구니에 있는 항목인지 (없으면 404)
    private void checkItemExists(Long userId, String cartItemId){
        if(cartRedisRepository.findQuantity(userId, cartItemId) == 0){
            throw new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
}
