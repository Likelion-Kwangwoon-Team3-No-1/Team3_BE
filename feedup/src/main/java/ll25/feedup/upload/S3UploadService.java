package ll25.feedup.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region}")
    private String region;

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    // 허용 MIME
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    // MIME -> 확장자 맵
    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png",  "png",
            "image/webp", "webp"
    );

    /* =====================
       Public API
       ===================== */

    /** 호스트 대표 사진 1장 업로드 */
    public String uploadHostThumbnail(MultipartFile file) {
        requireFile(file);
        String key = buildKey("hosts/thumbnail", extFor(file));
        putObject(key, file);
        return publicUrl(key);
    }

    /** 리뷰 사진 여러 장 업로드 */
    public List<String> uploadReviewPhotos(long promotionId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        String prefix = "reviews/promotion-" + promotionId;
        return files.stream()
                .filter(Objects::nonNull)
                .filter(f -> !f.isEmpty())
                .map(f -> {
                    validateFile(f);
                    String key = buildKey(prefix, extFor(f));
                    putObject(key, f);
                    return publicUrl(key);
                })
                .collect(Collectors.toList());
    }

    /*  Private Helpers */
    private void requireFile(MultipartFile f) {
        if (f == null || f.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대표 이미지를 첨부하세요.");
        }
        validateFile(f);
    }

    private void validateFile(MultipartFile f) {
        if (f.getSize() > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 5MB 초과");
        }
        String ct = contentTypeOf(f);
        if (!ALLOWED.contains(ct)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPEG/PNG/WEBP만 허용");
        }
    }

    private String contentTypeOf(MultipartFile f) {
        String ct = f.getContentType();
        if (ct == null || ct.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "알 수 없는 파일 형식");
        }
        return ct;
    }

    private String extFor(MultipartFile f) {
        String ct = contentTypeOf(f);
        String ext = EXT.get(ct);
        if (ext == null) {
            // ALLOWED 체크에서 이미 컷 되지만 방어로직 유지
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 확장자");
        }
        return ext;
    }

    private String buildKey(String prefix, String ext) {
        // prefix 끝에 / 없으면 붙여줌
        String p = prefix.endsWith("/") ? prefix : prefix + "/";
        return p + UUID.randomUUID() + "." + ext;
    }

    private void putObject(String key, MultipartFile file) {
        String ct = contentTypeOf(file);
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(ct)
                    .build();
            s3.putObject(put, RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", e);
        }
    }

    private String publicUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}