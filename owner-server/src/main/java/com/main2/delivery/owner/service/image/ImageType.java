package com.main2.delivery.owner.service.image;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

// 허용하는 이미지 형식. 확장자뿐 아니라 파일 앞부분 바이트(시그니처)로 실제 형식을 확인한다.
// Content-Type 헤더나 확장자는 클라이언트가 바꿀 수 있어서 그것만 믿으면 이미지가 아닌 파일이 올라온다.
public enum ImageType {
    JPEG("jpg", Set.of("jpg", "jpeg")),
    PNG("png", Set.of("png")),
    GIF("gif", Set.of("gif")),
    WEBP("webp", Set.of("webp"));

    private final String savedExtension;          // 저장할 때 붙일 확장자
    private final Set<String> allowedExtensions;  // 업로드 파일 이름에 허용하는 확장자

    ImageType(String savedExtension, Set<String> allowedExtensions) {
        this.savedExtension = savedExtension;
        this.allowedExtensions = allowedExtensions;
    }

    public String savedExtension() {
        return savedExtension;
    }

    public boolean allows(String extension) {
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }

    // 파일 앞부분 바이트로 형식 판별. 어느 것에도 맞지 않으면 empty
    public static Optional<ImageType> detect(byte[] bytes) {
        if (bytes == null) {
            return Optional.empty();
        }
        if (startsWith(bytes, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPEG);
        }
        if (startsWith(bytes, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(PNG);
        }
        if (startsWithAscii(bytes, 0, "GIF87a") || startsWithAscii(bytes, 0, "GIF89a")) {
            return Optional.of(GIF);
        }
        if (startsWithAscii(bytes, 0, "RIFF") && startsWithAscii(bytes, 8, "WEBP")) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] bytes, int offset, int... signature) {
        if (bytes.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[offset + i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithAscii(byte[] bytes, int offset, String text) {
        byte[] expected = text.getBytes(StandardCharsets.US_ASCII);
        return bytes.length >= offset + expected.length
                && Arrays.equals(bytes, offset, offset + expected.length, expected, 0, expected.length);
    }
}
