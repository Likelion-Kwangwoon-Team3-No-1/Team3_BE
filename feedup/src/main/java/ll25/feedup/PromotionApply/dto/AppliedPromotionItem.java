package ll25.feedup.PromotionApply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ll25.feedup.Host.domain.Host;
import ll25.feedup.Host.domain.PlaceCategory;
import ll25.feedup.Promotion.domain.Promotion;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AppliedPromotionItem {

    private Long promotionId;
    private String nickname;            // Host.nickname
    private PlaceCategory category;     // Host.category (enum)
    private String address;             // Host.address
    @JsonProperty("start_date")
    private String startDate;           // yyyy-MM-dd
    @JsonProperty("end_date")
    private String endDate;             // yyyy-MM-dd
    private String thumbnail;           // Host.thumbnail

    public static AppliedPromotionItem from(Promotion p) {
        Host h = p.getHost();

        AppliedPromotionItem dto = new AppliedPromotionItem();
        dto.promotionId = p.getId();
        dto.nickname    = h.getNickname();
        dto.category    = h.getCategory();
        dto.address     = h.getAddress();
        dto.startDate   = p.getStartDate().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        dto.endDate     = p.getEndDate().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        dto.thumbnail   = h.getThumbnail();
        return dto;
    }
}