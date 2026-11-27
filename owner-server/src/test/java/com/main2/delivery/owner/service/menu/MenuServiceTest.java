package com.main2.delivery.owner.service.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.owner.dto.menu.MenuCreateResponse;
import com.main2.delivery.owner.dto.menu.MenuRequest;
import com.main2.delivery.owner.dto.menu.MenuResponse;
import com.main2.delivery.owner.repository.menu.MenuRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;

/**
 * 메뉴 서비스 단위 테스트.
 * DB 대신 가짜 Repository(@Mock)를 써서, 로그인(JWT) 없이 ownerId만 넘겨 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    MenuService menuService;

    static final Long MY_OWNER_ID = 17L;
    static final Long OTHER_OWNER_ID = 99L;
    static final Long MY_STORE_ID = 10L;
    static final Long COLLECTED_STORE_ID = 5L;
    static final Long MY_MENU_ID = 101L;

    StoreEntity myStore;
    StoreEntity collectedStore;
    MenuEntity myMenu;

    @BeforeEach
    void setUp() {
        OwnerEntity me = OwnerEntity.builder()
                .phone("01011112222")
                .passwordHash("test-hash")
                .name("테스트업주")
                .build();
        setId(me, MY_OWNER_ID);

        myStore = StoreEntity.builder()
                .owner(me)
                .storeName("테스트치킨")
                .build();
        setId(myStore, MY_STORE_ID);

        collectedStore = StoreEntity.builder()
                .owner(null)                    // 수집 가게는 주인이 없음
                .storeName("수집가게")
                .build();
        setId(collectedStore, COLLECTED_STORE_ID);

        myMenu = MenuEntity.builder()
                .store(myStore)
                .menuName("후라이드")
                .description("바삭한 후라이드")
                .price(23000)
                .build();
        setId(myMenu, MY_MENU_ID);
    }

    @Nested
    @DisplayName("메뉴 목록")
    class FindMenus {

        @Test
        void 내_가게의_메뉴_목록을_돌려준다() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));
            given(menuRepository.findByStoreIdAndDeletedFalseOrderByIdAsc(MY_STORE_ID)).willReturn(List.of(myMenu));

            List<MenuResponse> result = menuService.findMenus(MY_OWNER_ID, MY_STORE_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMenuId()).isEqualTo(MY_MENU_ID);
            assertThat(result.get(0).getName()).isEqualTo("후라이드");
        }

        @Test
        void 다른_업주의_가게면_403() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));

            assertErrorCode(() -> menuService.findMenus(OTHER_OWNER_ID, MY_STORE_ID), CommonErrorCode.COMMON_FORBIDDEN);
        }

        @Test
        void 없는_가게면_404() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> menuService.findMenus(MY_OWNER_ID, MY_STORE_ID), CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 등록")
    class Create {

        @Test
        void 내_가게에_메뉴를_등록하고_번호를_돌려준다() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));
            // 진짜 DB라면 저장할 때 번호가 생기므로, 가짜 save에서 번호를 넣어 준다
            given(menuRepository.save(any(MenuEntity.class))).willAnswer(invocation -> {
                MenuEntity saved = invocation.getArgument(0);
                setId(saved, 200L);
                return saved;
            });

            MenuCreateResponse result = menuService.create(MY_OWNER_ID, MY_STORE_ID,
                    request("양념치킨", "달콤한 양념", 25000, null));

            ArgumentCaptor<MenuEntity> captor = ArgumentCaptor.forClass(MenuEntity.class);
            verify(menuRepository).save(captor.capture());
            MenuEntity saved = captor.getValue();

            assertThat(result.getMenuId()).isEqualTo(200L);
            assertThat(saved.getStore()).isSameAs(myStore);
            assertThat(saved.getMenuName()).isEqualTo("양념치킨");
            assertThat(saved.getDescription()).isEqualTo("달콤한 양념");
            assertThat(saved.getPrice()).isEqualTo(25000);
            assertThat(saved.isDeleted()).isFalse();
            assertThat(saved.isSoldOut()).isFalse();
        }

        @Test
        void 다른_업주의_가게에는_등록할_수_없다_403() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));

            assertErrorCode(() -> menuService.create(OTHER_OWNER_ID, MY_STORE_ID, request("양념치킨", null, 25000, null)),
                    CommonErrorCode.COMMON_FORBIDDEN);
            verify(menuRepository, never()).save(any());
        }

        @Test
        void 수집_가게에는_등록할_수_없다_403() {
            given(storeRepository.findById(COLLECTED_STORE_ID)).willReturn(Optional.of(collectedStore));

            assertErrorCode(() -> menuService.create(MY_OWNER_ID, COLLECTED_STORE_ID, request("양념치킨", null, 25000, null)),
                    CommonErrorCode.COMMON_FORBIDDEN);
            verify(menuRepository, never()).save(any());
        }

        @Test
        void 없는_가게에는_등록할_수_없다_404() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> menuService.create(MY_OWNER_ID, MY_STORE_ID, request("양념치킨", null, 25000, null)),
                    CommonErrorCode.COMMON_NOT_FOUND);
            verify(menuRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class FindMenu {

        @Test
        void 내_메뉴를_돌려준다() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            MenuResponse result = menuService.findMenu(MY_OWNER_ID, MY_MENU_ID);

            assertThat(result.getMenuId()).isEqualTo(MY_MENU_ID);
            assertThat(result.getPrice()).isEqualTo(23000);
        }

        @Test
        void 다른_업주의_메뉴면_403() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            assertErrorCode(() -> menuService.findMenu(OTHER_OWNER_ID, MY_MENU_ID), CommonErrorCode.COMMON_FORBIDDEN);
        }

        @Test
        void 없거나_삭제된_메뉴면_404() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> menuService.findMenu(MY_OWNER_ID, MY_MENU_ID), CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class Update {

        @Test
        void 이름_설명_가격_사진만_바뀐다() {
            myMenu.setSoldOut(true);
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            MenuResponse result = menuService.updateMenu(MY_OWNER_ID, MY_MENU_ID,
                    request("후라이드 반반", "반반 메뉴", 24000, "https://img.test/a.png"));

            assertThat(myMenu.getMenuName()).isEqualTo("후라이드 반반");
            assertThat(myMenu.getDescription()).isEqualTo("반반 메뉴");
            assertThat(myMenu.getPrice()).isEqualTo(24000);
            assertThat(myMenu.getImageUrl()).isEqualTo("https://img.test/a.png");
            assertThat(myMenu.isSoldOut()).isTrue();            // 수정 범위 밖은 그대로
            assertThat(myMenu.getStore()).isSameAs(myStore);    // 가게도 그대로
            assertThat(result.getName()).isEqualTo("후라이드 반반");
        }

        @Test
        void 다른_업주의_메뉴는_수정할_수_없다_403() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            assertErrorCode(() -> menuService.updateMenu(OTHER_OWNER_ID, MY_MENU_ID, request("해킹", null, 0, null)),
                    CommonErrorCode.COMMON_FORBIDDEN);
            assertThat(myMenu.getMenuName()).isEqualTo("후라이드");
        }

        @Test
        void 없거나_삭제된_메뉴는_수정할_수_없다_404() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> menuService.updateMenu(MY_OWNER_ID, MY_MENU_ID, request("후라이드", null, 1000, null)),
                    CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class Delete {

        @Test
        void 내_메뉴는_삭제_표시된다() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            menuService.deleteMenu(MY_OWNER_ID, MY_MENU_ID);

            assertThat(myMenu.isDeleted()).isTrue();
            verify(menuRepository, never()).delete(any());      // 실제 삭제는 하지 않음
        }

        @Test
        void 다른_업주의_메뉴는_삭제할_수_없다_403() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.of(myMenu));

            assertErrorCode(() -> menuService.deleteMenu(OTHER_OWNER_ID, MY_MENU_ID), CommonErrorCode.COMMON_FORBIDDEN);
            assertThat(myMenu.isDeleted()).isFalse();
        }

        @Test
        void 없거나_이미_삭제된_메뉴는_404() {
            given(menuRepository.findByIdAndDeletedFalse(MY_MENU_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> menuService.deleteMenu(MY_OWNER_ID, MY_MENU_ID), CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    // ===== 도우미 =====

    // 엔티티 id는 Setter가 없고 DB가 정해 주므로, 테스트에서만 직접 넣는다
    private static void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    private static MenuRequest request(String name, String description, Integer price, String imageUrl) {
        MenuRequest req = new MenuRequest();
        req.setName(name);
        req.setDescription(description);
        req.setPrice(price);
        req.setImageUrl(imageUrl);
        return req;
    }

    private static void assertErrorCode(Runnable action, CommonErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }
}
