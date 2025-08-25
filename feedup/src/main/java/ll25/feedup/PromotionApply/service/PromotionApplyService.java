package ll25.feedup.PromotionApply.service;

import ll25.feedup.Mate.domain.Mate;
import ll25.feedup.Mate.repository.MateRepository;
import ll25.feedup.Promotion.domain.Promotion;
import ll25.feedup.Promotion.repository.PromotionRepository;
import ll25.feedup.PromotionApply.domain.PromotionApply;
import ll25.feedup.PromotionApply.dto.AppliedPromotionItem;
import ll25.feedup.PromotionApply.dto.MyAppliedPromotionsResponse;
import ll25.feedup.PromotionApply.dto.PromotionApplyResponse;
import ll25.feedup.PromotionApply.repository.PromotionApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionApplyService {

    private final PromotionRepository promotionRepository;
    private final PromotionApplyRepository promotionApplyRepository;
    private final MateRepository mateRepository;

    @Transactional
    public PromotionApplyResponse apply (String mateLoginId, Long promotionId) {

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> notFound("Promotion(id)", promotionId.toString()));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw badRequest("프로모션 기간이 아닙니다.");
        }

        Mate mate = mateRepository.findByLoginId(mateLoginId)
                .orElseThrow(() -> notFound("Mate(loginId)", mateLoginId));

        if (promotionApplyRepository.existsByPromotionAndMate(promotion, mate)) {
            throw conflict("이미 신청한 프로모션입니다.");
        }

        int capacity = promotion.getPlan().getTeamLimit();
        long appliedCount = promotionApplyRepository.countByPromotion(promotion);
        if (appliedCount >= capacity) {
            throw conflict("신청 정원이 가득 찼습니다.");
        }

        PromotionApply saved = promotionApplyRepository.save(
                new PromotionApply(promotion, mate, now)
        );

        return new PromotionApplyResponse(
                saved.getId(),
                promotion.getId(),           // 숫자 그대로
                mateLoginId,
                saved.getAppliedAt().toString(),
                saved.isReviewWritten()
        );
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseStatusException notFound(String field, String val) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, field + "가 존재하지 않습니다: " + val);
    }

    private ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }

    @Transactional(readOnly = true)
    public MyAppliedPromotionsResponse getMyAppliedPromotions(String mateLoginId, int offset, int limit) {
        Mate mate = mateRepository.findByLoginId(mateLoginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        int safeLimit  = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        int pageIndex  = safeOffset / safeLimit;

        Page<PromotionApply> page = promotionApplyRepository.findByMateOrderByAppliedAtDesc(
                mate, PageRequest.of(pageIndex, safeLimit)
        );

        List<AppliedPromotionItem> items = page.getContent().stream()
                .map(pa -> AppliedPromotionItem.from(pa.getPromotion()))
                .toList();

        int nextOffset = page.hasNext() ? safeOffset + safeLimit : safeOffset;

        return new MyAppliedPromotionsResponse(items, page.hasNext(), nextOffset);
    }
}