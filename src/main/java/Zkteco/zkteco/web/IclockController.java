package Zkteco.zkteco.web;

import Zkteco.zkteco.modules.iclock.service.IclockProtocolService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iclock")
public class IclockController {

    private final IclockProtocolService protocolService;

    public IclockController(IclockProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return plainText("OK");
    }

    @GetMapping("/cdata")
    public ResponseEntity<String> cdataGet(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleCdata(normalizeParams(params), "", resolveRemoteIp(request));
        return plainText(payload);
    }

    @PostMapping(value = "/cdata", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> cdataPost(
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String body,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleCdata(normalizeParams(params), body, resolveRemoteIp(request));
        return plainText(payload);
    }

    @GetMapping("/getrequest")
    public ResponseEntity<String> getRequest(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleGetRequest(normalizeParams(params), resolveRemoteIp(request));
        return plainText(payload);
    }

    @GetMapping("/devicecmd")
    public ResponseEntity<String> deviceCmdGet(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleDeviceCmd(normalizeParams(params), "", resolveRemoteIp(request), true);
        return plainText(payload);
    }

    @PostMapping(value = "/devicecmd", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> deviceCmdPost(
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String body,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleDeviceCmd(normalizeParams(params), body, resolveRemoteIp(request), false);
        return plainText(payload);
    }

    @GetMapping("/fdata")
    public ResponseEntity<String> fdata(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleFdata(normalizeParams(params), "", resolveRemoteIp(request));
        return plainText(payload);
    }

    @PostMapping(value = "/fdata", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> fdataPost(
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String body,
            HttpServletRequest request
    ) {
        String payload = protocolService.handleFdata(normalizeParams(params), body, resolveRemoteIp(request));
        return plainText(payload);
    }

    private Map<String, String> normalizeParams(Map<String, String> params) {
        Map<String, String> normalized = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            normalized.put(k, v);
            normalized.put(k.toUpperCase(), v);
        });
        return normalized;
    }

    private String resolveRemoteIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }
        return request.getRemoteAddr();
    }

    private ResponseEntity<String> plainText(String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(body);
    }
}
