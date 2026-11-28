package com.main2.delivery.owner.service.image;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// 로컬 폴더 저장: {upload-dir}/yyyy/MM/dd/{UUID}.{ext}
// 파일 이름은 서버가 새로 지으므로 업로드 파일 이름에 ../ 등이 있어도 다른 경로에 쓰이지 않는다.
@Slf4j
@Component
public class LocalImageStorage implements ImageStorage {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path rootDir;

    public LocalImageStorage(@Value("${app.image.upload-dir}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String save(byte[] bytes, String extension) {
        String relativePath = LocalDate.now().format(DATE_PATH) + "/" + UUID.randomUUID() + "." + extension;
        Path target = rootDir.resolve(relativePath).normalize();
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            log.error("이미지 저장 실패 path={}", target, e);
            throw new BusinessException(OwnerErrorCode.IMAGE_SAVE_FAILED);
        }
        return relativePath;
    }
}
