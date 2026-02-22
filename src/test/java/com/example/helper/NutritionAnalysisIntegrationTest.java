
package com.example.helper;

import com.example.helper.Dto.StreamResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("营养分析API集成测试")
public class NutritionAnalysisIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/nutrition";
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private NutritionAnalysisRequestDTO createValidRequest() {
        NutritionAnalysisRequestDTO request = new NutritionAnalysisRequestDTO();
        request.setFoodDescription("今天早餐吃了两个鸡蛋和一杯牛奶");
        request.setAge(25);
        request.setGender("男");
        request.setHeight(175.0);
        request.setWeight(70.0);
        request.setActivityLevel(3);
        request.setSpecialNeeds("减肥");
        request.setAllergies("海鲜,坚果");
        return request;
    }

    @Test
    @DisplayName("测试单次营养分析接口 - 成功场景")
    void testAnalyzeNutrition_Success() {
        // 准备测试数据
        NutritionAnalysisRequestDTO request = createValidRequest();

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NutritionAnalysisRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/analyze",
                HttpMethod.POST,
                entity,
                String.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // 验证响应体结构
        try {
            // 解析响应
            var responseJson = objectMapper.readTree(response.getBody());
            assertTrue(responseJson.has("code"));
            assertTrue(responseJson.has("message"));
            assertTrue(responseJson.has("data"));

            // 验证数据部分
            var data = responseJson.get("data");
            assertTrue(data.has("analysisResult"));
            assertTrue(data.has("nutritionAdvice"));
            assertTrue(data.has("riskWarning"));

            // 验证分析结果
            var analysisResult = data.get("analysisResult");
            assertTrue(analysisResult.has("totalCalories"));
            assertTrue(analysisResult.has("protein"));
            assertTrue(analysisResult.has("fat"));
            assertTrue(analysisResult.has("carbohydrates"));
            assertTrue(analysisResult.has("fiber"));
            assertTrue(analysisResult.has("mainNutrientsAnalysis"));

            // 验证营养建议
            var nutritionAdvice = data.get("nutritionAdvice");
            assertTrue(nutritionAdvice.has("calorieAdvice"));
            assertTrue(nutritionAdvice.has("nutrientBalanceAdvice"));
            assertTrue(nutritionAdvice.has("improvementSuggestions"));
            assertTrue(nutritionAdvice.has("foodSubstitutionAdvice"));

            // 验证风险提醒
            var riskWarning = data.get("riskWarning");
            assertTrue(riskWarning.has("hasRisk"));
            assertTrue(riskWarning.has("riskType"));
            assertTrue(riskWarning.has("riskDescription"));
            assertTrue(riskWarning.has("preventionAdvice"));

        } catch (Exception e) {
            fail("解析响应失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试单次营养分析接口 - 验证失败场景")
    void testAnalyzeNutrition_ValidationError() {
        // 准备无效请求（缺少必填字段）
        NutritionAnalysisRequestDTO request = new NutritionAnalysisRequestDTO();
        // 不设置foodDescription，应该触发验证错误

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NutritionAnalysisRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/analyze",
                HttpMethod.POST,
                entity,
                String.class
        );

        // 验证响应状态码
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("测试流式营养分析接口")
    void testAnalyzeNutritionStream_Success() {
        NutritionAnalysisRequestDTO request = createValidRequest();

        Flux<String> responseFlux = webClient.post()
                .uri("/analyze/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(30));

        // 收集所有响应
        StringBuilder fullResponse = new StringBuilder();
        String[] finalResponse = new String[1];
        boolean[] hasDone = {false};
        boolean[] hasError = {false};

        responseFlux.subscribe(
                chunk -> {
                    fullResponse.append(chunk);
                    try {
                        // 解析每个chunk
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6);
                            StreamResponseDTO dto = objectMapper.readValue(data, StreamResponseDTO.class);

                            if (dto.getDone() != null && dto.getDone()) {
                                hasDone[0] = true;
                            }
                            if (dto.getError() != null) {
                                hasError[0] = true;
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误，继续处理下一个chunk
                    }
                },
                error -> fail("流式请求失败: " + error.getMessage()),
                () -> {
                    finalResponse[0] = fullResponse.toString();
                }
        );

        // 等待流式响应完成
        try {
            Thread.sleep(10000); // 给足够的时间让流式响应完成
        } catch (InterruptedException e) {
            fail("测试被中断");
        }

        // 验证结果
        assertNotNull(finalResponse[0]);
        assertFalse(finalResponse[0].isEmpty());
        assertTrue(hasDone[0], "流式响应应该包含done信号");
        assertFalse(hasError[0], "流式响应不应该包含错误");
    }

    @Test
    @DisplayName("测试多轮营养分析接口 - 创建新会话")
    void testAnalyzeWithContext_NewSession() {
        String userId = "test-user-" + UUID.randomUUID();
        NutritionAnalysisRequestDTO request = createValidRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NutritionAnalysisRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=true",
                HttpMethod.POST,
                entity,
                String.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try {
            var responseJson = objectMapper.readTree(response.getBody());
            assertTrue(responseJson.has("data"));
            var data = responseJson.get("data");
            assertTrue(data.has("analysisResult"));
            assertTrue(data.has("nutritionAdvice"));
            assertTrue(data.has("riskWarning"));
        } catch (Exception e) {
            fail("解析响应失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试多轮营养分析接口 - 继续会话")
    void testAnalyzeWithContext_ContinueSession() {
        String userId = "test-user-" + UUID.randomUUID();
        NutritionAnalysisRequestDTO request = createValidRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NutritionAnalysisRequestDTO> entity = new HttpEntity<>(request, headers);

        // 第一次请求 - 创建新会话
        ResponseEntity<String> firstResponse = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=true",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());

        // 第二次请求 - 继续会话
        request.setFoodDescription("午餐吃了一份沙拉和鸡胸肉");
        ResponseEntity<String> secondResponse = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=false",
                HttpMethod.POST,
                entity,
                String.class
        );

        // 验证第二次请求
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        try {
            var responseJson = objectMapper.readTree(secondResponse.getBody());
            assertTrue(responseJson.has("data"));
            var data = responseJson.get("data");
            assertTrue(data.has("analysisResult"));
            assertTrue(data.has("nutritionAdvice"));
            assertTrue(data.has("riskWarning"));
        } catch (Exception e) {
            fail("解析响应失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试多轮流式营养分析接口")
    void testAnalyzeWithContextStream_Success() {
        String userId = "test-user-" + UUID.randomUUID();
        NutritionAnalysisRequestDTO request = createValidRequest();

        Flux<String> responseFlux = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/analyze/contextual/stream")
                        .queryParam("userId", userId)
                        .queryParam("isNewSession", "true")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(30));

        // 收集所有响应
        StringBuilder fullResponse = new StringBuilder();
        String[] finalResponse = new String[1];
        boolean[] hasDone = {false};
        boolean[] hasError = {false};

        responseFlux.subscribe(
                chunk -> {
                    fullResponse.append(chunk);
                    try {
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6);
                            StreamResponseDTO dto = objectMapper.readValue(data, StreamResponseDTO.class);

                            if (dto.getDone() != null && dto.getDone()) {
                                hasDone[0] = true;
                            }
                            if (dto.getError() != null) {
                                hasError[0] = true;
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                },
                error -> fail("流式请求失败: " + error.getMessage()),
                () -> finalResponse[0] = fullResponse.toString()
        );

        // 等待流式响应完成
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            fail("测试被中断");
        }

        // 验证结果
        assertNotNull(finalResponse[0]);
        assertFalse(finalResponse[0].isEmpty());
        assertTrue(hasDone[0], "流式响应应该包含done信号");
        assertFalse(hasError[0], "流式响应不应该包含错误");
    }

    @Test
    @DisplayName("测试结束会话接口")
    void testEndSession_Success() {
        String sessionId = UUID.randomUUID().toString();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/session/" + sessionId,
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try {
            var responseJson = objectMapper.readTree(response.getBody());
            assertTrue(responseJson.has("code"));
            assertTrue(responseJson.has("message"));
        } catch (Exception e) {
            fail("解析响应失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试多轮对话 - 完整流程")
    void testMultiTurnConversation_CompleteFlow() {
        String userId = "test-user-" + UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 第一轮对话
        NutritionAnalysisRequestDTO request1 = createValidRequest();
        HttpEntity<NutritionAnalysisRequestDTO> entity1 = new HttpEntity<>(request1, headers);
        ResponseEntity<String> response1 = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=true",
                HttpMethod.POST,
                entity1,
                String.class
        );

        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // 第二轮对话
        NutritionAnalysisRequestDTO request2 = createValidRequest();
        request2.setFoodDescription("午餐吃了一份沙拉和鸡胸肉");
        HttpEntity<NutritionAnalysisRequestDTO> entity2 = new HttpEntity<>(request2, headers);
        ResponseEntity<String> response2 = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=false",
                HttpMethod.POST,
                entity2,
                String.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());

        // 第三轮对话
        NutritionAnalysisRequestDTO request3 = createValidRequest();
        request3.setFoodDescription("晚餐吃了一些水果和酸奶");
        HttpEntity<NutritionAnalysisRequestDTO> entity3 = new HttpEntity<>(request3, headers);
        ResponseEntity<String> response3 = restTemplate.exchange(
                baseUrl + "/analyze/contextual?userId=" + userId + "&isNewSession=false",
                HttpMethod.POST,
                entity3,
                String.class
        );

        assertEquals(HttpStatus.OK, response3.getStatusCode());

        // 验证所有响应都包含必要的数据
        for (ResponseEntity<String> response : new ResponseEntity[]{response1, response2, response3}) {
            try {
                var responseJson = objectMapper.readTree(response.getBody());
                assertTrue(responseJson.has("data"));
                var data = responseJson.get("data");
                assertTrue(data.has("analysisResult"));
                assertTrue(data.has("nutritionAdvice"));
                assertTrue(data.has("riskWarning"));
            } catch (Exception e) {
                fail("解析响应失败: " + e.getMessage());
            }
        }
    }
}
