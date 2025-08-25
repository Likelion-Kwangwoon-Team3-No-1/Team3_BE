package ll25.feedup.PromotionApply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyAppliedPromotionsResponse {
    private List<AppliedPromotionItem> items;
    private boolean hasNext;
    private int nextOffset;
}