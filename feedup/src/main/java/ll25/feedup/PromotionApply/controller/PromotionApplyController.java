package ll25.feedup.PromotionApply.controller;

import jakarta.validation.Valid;
import ll25.feedup.PromotionApply.dto.MyAppliedPromotionsResponse;
import ll25.feedup.PromotionApply.dto.PromotionApplyCreateRequest;
import ll25.feedup.PromotionApply.dto.PromotionApplyResponse;
import ll25.feedup.PromotionApply.service.PromotionApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotion-applies")
public class PromotionApplyController {

    private final PromotionApplyService promotionApplyService;

    @PostMapping
    public ResponseEntity<?> apply(
            Authentication authentication,
            @Valid @RequestBody PromotionApplyCreateRequest request
    ) {
        String mateLoginId = authentication.getName();

        try {
            Long promotionId = Long.parseLong(request.getPromotionId());
            PromotionApplyResponse response = promotionApplyService.apply(mateLoginId, promotionId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"promotionId는 숫자여야 합니다: " + request.getPromotionId());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<MyAppliedPromotionsResponse> getMyAppliedPromotions(
            Authentication auth,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        final String mateLoginId = auth.getName();
        return ResponseEntity.ok(promotionApplyService.getMyAppliedPromotions(mateLoginId, offset, limit));
    }


}
