package com.main2.delivery.owner.service.image;

// 이미지 저장소. 지금은 로컬 폴더(LocalImageStorage), 이후 S3 등 클라우드로 바꿀 때 구현체만 교체한다.
public interface ImageStorage {

    // 바이트를 저장하고, 공개 URL 뒤에 붙일 경로를 돌려준다 (예: 2026/11/28/uuid.png)
    String save(byte[] bytes, String extension);
}
