package ll25.feedup.Host.controller;

import ll25.feedup.Host.dto.HostMeResponse;
import ll25.feedup.Host.service.HostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hosts")
public class HostController {

    private final HostQueryService hostQueryService;

    /** 로그인한 Host의 기본 정보 조회 */
    @GetMapping("/info")
    public ResponseEntity<HostMeResponse> getHostInfo(
            @AuthenticationPrincipal String loginId
    ) {
        HostMeResponse body = hostQueryService.getHostInfo(loginId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}