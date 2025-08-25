package ll25.feedup.controller;

import jakarta.validation.Valid;
import ll25.feedup.Promotion.dto.PromotionCreateRequest;
import ll25.feedup.Promotion.dto.PromotionCreateResponse;
import ll25.feedup.Promotion.dto.PromotionDetailResponse;
import ll25.feedup.Promotion.dto.PromotionListResponse;
import ll25.feedup.Promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    /** 1) 프로모션 생성 (로그인한 호스트 기준) */
    @PostMapping
    public ResponseEntity<PromotionCreateResponse> createPromotion(
            @AuthenticationPrincipal String loginId,
            @Valid @RequestBody PromotionCreateRequest req
    ) {
        PromotionCreateResponse res = promotionService.createPromotion(loginId, req);
        return ResponseEntity
                .created(URI.create("/api/promotions/" + res.getPromotionId()))
                .body(res);
    }

    /** 2) 프로모션 리스트 조회: status 파라미터 분기 */
    @GetMapping
    public ResponseEntity<PromotionListResponse> getPromotions(
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "endDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if ("completed".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(promotionService.getCompletedPromotions(pageable));
        } else if ("active".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(promotionService.getOpenPromotions(pageable));
        }
        return ResponseEntity.badRequest().build();
    }

    /** 3) 로그인한 호스트의 전체 프로모션(상태 무관) */
    @GetMapping("/me")
    public ResponseEntity<PromotionListResponse> getMyPromotions(
            @AuthenticationPrincipal String loginId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return ResponseEntity.ok(promotionService.getHostPromotionsByLoginId(loginId, pageable));
    }

    /** 4) 단건 상세 */
    @GetMapping("/{promotionId:\\d+}")
    public ResponseEntity<PromotionDetailResponse> getPromotionDetail(
            @PathVariable Long promotionId
    ) {
        return ResponseEntity.ok(promotionService.getPromotionDetail(promotionId));
    }
}