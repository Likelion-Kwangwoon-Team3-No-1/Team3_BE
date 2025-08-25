package ll25.feedup.Host.dto;

import ll25.feedup.Host.domain.Host;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HostMeResponse {
    private final String nickname;
    private final String phone;
    private final String address;
    private final String category;

    public static HostMeResponse from(Host h) {
        String categoryLabel = (h.getCategory() != null) ? h.getCategory().getLabel() : "기타";
        return new HostMeResponse(
                h.getNickname(),
                h.getPhone(),
                h.getAddress(),
                categoryLabel
        );
    }
}
