package ll25.feedup.PromotionApply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromotionApplyResponse {
    private Long applyId;
    private Long promotionId;
    private String mateLoginId;
    private String appliedAt;
    private boolean reviewWritten;
}