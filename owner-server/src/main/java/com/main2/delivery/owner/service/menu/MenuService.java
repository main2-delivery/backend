package com.main2.delivery.owner.service.menu;

import java.util.List;

import org.springframework.stereotype.Service;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.owner.dto.menu.MenuCreateResponse;
import com.main2.delivery.owner.dto.menu.MenuFlagRequest;
import com.main2.delivery.owner.dto.menu.MenuRequest;
import com.main2.delivery.owner.dto.menu.MenuResponse;
import com.main2.delivery.owner.repository.menu.MenuRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MenuService {
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    
    public List<MenuResponse> findMenus(Long ownerId, Long storeId){
        findMyStore(ownerId, storeId);

        return menuRepository.findByStoreIdAndDeletedFalseOrderByIdAsc(storeId).stream().map(MenuResponse::new).toList();
    }

    public MenuResponse findMenu(Long ownerId, Long menuId) {
        return new MenuResponse(findMyMenu(ownerId, menuId));
    }



    public MenuCreateResponse create(Long ownerId, Long storeId, MenuRequest req){
        StoreEntity storeEntity = findMyStore(ownerId, storeId);

        MenuEntity menuEntity = MenuEntity.builder()
                                    .store(storeEntity)
                                    .menuName(req.getName())
                                    .description(req.getDescription())
                                    .price(req.getPrice())
                                    .imageUrl(req.getImageUrl())
                                    .build();
        menuRepository.save(menuEntity);

        return new MenuCreateResponse(menuEntity.getId());
    }

    @Transactional
    public MenuResponse updateMenu(Long ownerId, Long menuId, MenuRequest req) {
        MenuEntity menu = findMyMenu(ownerId, menuId);
        menu.setMenuName(req.getName());
        menu.setDescription(req.getDescription());
        menu.setPrice(req.getPrice());
        menu.setImageUrl(req.getImageUrl());
        return new MenuResponse(menu);
    }

    @Transactional
    public void deleteMenu(Long ownerId, Long menuId) {
        MenuEntity menuEntity = findMyMenu(ownerId, menuId);
        menuEntity.setDeleted(true);
    }

    @Transactional
    public MenuResponse changeSoldOut(Long ownerId, Long menuId, MenuFlagRequest req){
        MenuEntity menuEntity = findMyMenu(ownerId, menuId);
        menuEntity.setSoldOut(req.getValue());
        return new MenuResponse(menuEntity);
    }

    @Transactional
    public MenuResponse changeHidden(Long ownerId, Long menuId, MenuFlagRequest req){
        MenuEntity menuEntity = findMyMenu(ownerId, menuId);
        menuEntity.setHidden(req.getValue());
        return new MenuResponse(menuEntity);
    }

    @Transactional
    public MenuResponse changeRepresentative(Long ownerId, Long menuId, MenuFlagRequest req){
        MenuEntity menuEntity = findMyMenu(ownerId, menuId);
        menuEntity.setRepresentative(req.getValue());
        return new MenuResponse(menuEntity);
    }

    // 메뉴를 복사해서 저장까지 하는 구조 (메뉴 복사의 목적이 무엇인지?)
    @Transactional
    public MenuCreateResponse copyMenu(Long ownerId, Long menuId){
        MenuEntity menuEntity = findMyMenu(ownerId, menuId);

        MenuEntity copy = MenuEntity.builder()
                            .store(menuEntity.getStore())
                            .menuName(menuEntity.getMenuName())
                            .description(menuEntity.getDescription())
                            .price(menuEntity.getPrice())
                            .imageUrl(menuEntity.getImageUrl())
                            .build();

        return new MenuCreateResponse(menuRepository.save(copy).getId());
    }

    // 내 가게 찾기: 없으면 404, 남의 가게면 403
    private StoreEntity findMyStore(Long ownerId, Long storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "가게를 찾을 수 없습니다."));
        checkOwner(store, ownerId);
        return store;
    }

    // 수집 가게(owner = null)는 누구의 가게도 아님
    private void checkOwner(StoreEntity store, Long ownerId) {
        OwnerEntity owner = store.getOwner();
        if (owner == null || !owner.getId().equals(ownerId)) {
            throw new BusinessException(CommonErrorCode.COMMON_FORBIDDEN, "내 가게가 아닙니다.");
        }
    }
    // 내 메뉴 찾기: 없거나 삭제됐으면 404, 남의 가게 메뉴면 403
    private MenuEntity findMyMenu(Long ownerId, Long menuId) {
        MenuEntity menu = menuRepository.findByIdAndDeletedFalse(menuId)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "메뉴를 찾을 수 없습니다."));
        checkOwner(menu.getStore(), ownerId);
        return menu;
    }
}
