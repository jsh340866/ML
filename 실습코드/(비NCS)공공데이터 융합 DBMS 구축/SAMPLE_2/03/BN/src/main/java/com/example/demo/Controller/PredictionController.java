package com.example.demo.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 예측 프록시 컨트롤러
 *  FN(React) -> BN(Spring Boot) -> ML(FastAPI, SAMPLE 02) -> model.pkl
 *
 *  - /api/spec        : ML 입력 정의(폼 자동 생성용)
 *  - /api/predict     : 예측만 (저장 안 함)
 *  - /api/predictions : 예측 이력 CRUD (ML 로 그대로 위임)
 *
 *  모든 경로가 SecurityConfig 의 anyRequest().authenticated() 대상 = 로그인해야 호출된다.
 */
@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class PredictionController {

    // docker-compose 의 BN 환경변수 ML_URL 로 주입 (기본값: ml-container:8002)
    @Value("${ml.url:http://ml-container:8002}")
    private String mlUrl;

    private final RestTemplate rt = new RestTemplate();

    /** ML 로 요청을 넘기고 응답(상태코드 포함)을 그대로 돌려준다 */
    private ResponseEntity<String> forward(HttpMethod method, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        try {
            return rt.exchange(mlUrl + path, method, request, String.class);
        } catch (HttpStatusCodeException e) {
            // ML 이 404 를 주면 BN 도 404 를 준다 (500 으로 뭉개지 않는다)
            log.warn("[ML {}] {} -> {}", method, path, e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    /** FN 은 features 만 보낸다 -> ML 이 요구하는 {"features": {...}} 형태로 감싼다 */
    private Map<String, Object> wrap(Map<String, Object> features) {
        Map<String, Object> body = new HashMap<>();
        body.put("features", features);
        return body;
    }

    @GetMapping("/api/spec")
    public ResponseEntity<String> spec() {
        return forward(HttpMethod.GET, "/spec", null);
    }

    @PostMapping("/api/predict")
    public ResponseEntity<String> predict(@RequestBody Map<String, Object> features) {
        log.info("[predict] features={}", features);
        return forward(HttpMethod.POST, "/predict", wrap(features));
    }

    // ---------------- 예측 이력 CRUD ----------------

    /** C : 예측 후 이력 저장 */
    @PostMapping("/api/predictions")
    public ResponseEntity<String> create(@RequestBody Map<String, Object> features) {
        log.info("[predictions:create] features={}", features);
        return forward(HttpMethod.POST, "/predictions", wrap(features));
    }

    /** R : 이력 목록 */
    @GetMapping("/api/predictions")
    public ResponseEntity<String> list(@RequestParam(defaultValue = "50") int limit) {
        return forward(HttpMethod.GET, "/predictions?limit=" + limit, null);
    }

    /** R : 이력 단건 */
    @GetMapping("/api/predictions/{id}")
    public ResponseEntity<String> get(@PathVariable int id) {
        return forward(HttpMethod.GET, "/predictions/" + id, null);
    }

    /** U : 입력을 바꿔 다시 예측하고 갱신 */
    @PutMapping("/api/predictions/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody Map<String, Object> features) {
        log.info("[predictions:update] id={} features={}", id, features);
        return forward(HttpMethod.PUT, "/predictions/" + id, wrap(features));
    }

    /** D : 이력 삭제 */
    @DeleteMapping("/api/predictions/{id}")
    public ResponseEntity<String> delete(@PathVariable int id) {
        log.info("[predictions:delete] id={}", id);
        return forward(HttpMethod.DELETE, "/predictions/" + id, null);
    }
}
