package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Setter
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Setter
    @Column(length = 500)
    private String description;

    @Setter
    private Integer price;          // null 이면 가격 미표기 (주문 불가)

    @Setter
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Setter
    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Setter
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Setter
    @Column(name = "is_representative", nullable = false)
    private boolean representative;

    @Setter
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;        // 삭제 표시 (주문 이력 보존)

    // 생성은 빌더로만 (MenuEntity.builder()...build()). soldOut·hidden·representative·deleted 는 false 로 시작
    @Builder
    private MenuEntity(
            StoreEntity store,
            String menuName,
            String description,
            Integer price,
            String imageUrl
    ) {
        this.store = store;
        this.menuName = menuName;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
