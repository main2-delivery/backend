package com.main2.delivery.customer.repository.cart;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

// 장바구니는 DB가 아니라 Redis에 저장
// 키: cart:{userId}  (Hash)
//   store            → 가게 번호
//   item:{cartItemId} → 수량
@Repository 
@RequiredArgsConstructor 
public class CartRedisRepository {
    private static final Duration TTL = Duration.ofHours(24);   // 마지막으로 건드린 24시간 지나면 삭제
    private static final String STORE_FIELD = "store";
    private static final String ITEM_PREFIX = "item:";

    private final StringRedisTemplate redis;

    // 장바구니에 담긴 가게 번호 (비어있으면 null)
    public Long findStoreId(Long userId){
        Object value = redis.opsForHash().get(key(userId), STORE_FIELD);
        return value == null ? null : Long.valueOf(value.toString());
    }

    // 담긴 항목 전부 (cartItemId -> 수량)
    public Map<String, Integer> findItems(Long userId){
        Map<String, Integer> items = new LinkedHashMap<>();
        Map<Object, Object> entries = redis.opsForHash().entries(key(userId));
        for(Map.Entry<Object, Object> entry : entries.entrySet()){
            String field = entry.getKey().toString();
            if(field.startsWith(ITEM_PREFIX)){
                items.put(field.substring(ITEM_PREFIX.length()), Integer.valueOf(entry.getValue().toString()));
            }
        }
        return items;
    }

    // 항목 하나의 수량 (없으면 0)
    public int findQuantity(Long userId, String cartItemId){
        Object value = redis.opsForHash().get(key(userId), itemField(cartItemId));
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    // 수량 더하기(없던 항목이면 새로 생김) HINCRBY라 동시에 눌러도 수량이 안씹힘
    public void addQuantity(Long userId, Long storeId, String cartItemId, int quantity) {
        String key = key(userId);
        redis.opsForHash().put(key, STORE_FIELD, storeId.toString());
        redis.opsForHash().increment(key, itemField(cartItemId), quantity);
        redis.expire(key, TTL);
    }

    // 수량 바꾸기
    public void setQuantity(Long userId, String cartItemId, int quantity) {
        String key = key(userId);
        redis.opsForHash().put(key, itemField(cartItemId), String.valueOf(quantity));
        redis.expire(key, TTL);
    }

    // 항목 하나 삭제 (가게 번호만 남으면 장바구니 통째로 삭제)
    public void removeItem(Long userId, String cartItemId) {
        String key = key(userId);
        redis.opsForHash().delete(key, itemField(cartItemId));
        if (redis.opsForHash().size(key) <= 1) {
            redis.delete(key);
        }
    }

    // 전체 비우기
    public void clear(Long userId) {
        redis.delete(key(userId));
    }

    private String key(Long userId) {
        return "cart:" + userId;
    }

    private String itemField(String cartItemId) {
        return ITEM_PREFIX + cartItemId;
    }
}
