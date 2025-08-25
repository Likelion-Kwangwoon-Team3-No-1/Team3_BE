package ll25.feedup.upload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3UploadService s3UploadService;

    @PostMapping(value = "/host-thumbnail", consumes = "multipart/form-data")
    public ResponseEntity<UploadPhotosResponse> uploadHostThumbnail(
            @RequestPart("thumbnail") MultipartFile thumbnail
    ) {
        String url = s3UploadService.uploadHostThumbnail(thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UploadPhotosResponse(List.of(url)));
    }


    @PostMapping(value = "/review-photos", consumes = "multipart/form-data")
    public ResponseEntity<UploadPhotosResponse> uploadReviewPhotos(
            Authentication auth,
            @RequestParam("promotionId") long promotionId,
            @RequestPart("photos") List<MultipartFile> photos
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        List<String> urls = s3UploadService.uploadReviewPhotos(promotionId, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadPhotosResponse(urls));
    }

    @Getter
    @AllArgsConstructor
    public static class UploadPhotosResponse {
        private List<String> urls;
    }
}
