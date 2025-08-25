package ll25.feedup.controller;

import jakarta.validation.Valid;
import ll25.feedup.Review.dto.MyReviewsResponse;
import ll25.feedup.Review.dto.ReviewCreateRequest;
import ll25.feedup.Review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> createReviewJson(
            Authentication authentication,
            @Valid @RequestBody ReviewCreateRequest request
    ){
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        reviewService.createReview(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<MyReviewsResponse> getMyReviews(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        final String loginId = auth.getName();
        MyReviewsResponse body = reviewService.getMyReviews(loginId, 0, 20);
        return ResponseEntity.ok(body);
    }

    @GetMapping(params = "promotionId")
    public ResponseEntity<MyReviewsResponse> getPromotionReviews(
            @RequestParam Long promotionId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByPromotion(promotionId, offset, limit));
    }
}