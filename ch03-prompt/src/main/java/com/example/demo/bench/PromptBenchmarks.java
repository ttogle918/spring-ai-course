package com.example.demo.bench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.service.AiServiceChainOfThoughtPrompt;
import com.example.demo.service.AiServiceDefaultMethod;
import com.example.demo.service.AiServiceFewShotPrompt;
import com.example.demo.service.AiServiceMultiMessages;
import com.example.demo.service.AiServicePromptTemplate;
import com.example.demo.service.AiServiceRoleAssignmentPrompt;
import com.example.demo.service.AiServiceSelfConsistency;
import com.example.demo.service.AiServiceStepBackPrompt;
import com.example.demo.service.AiServiceZeroShotPrompt;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import java.lang.reflect.Method;

@Component
public class PromptBenchmarks implements CommandLineRunner {

    @Autowired
    private AiServiceZeroShotPrompt zeroShot;

    @Autowired
    private AiServiceFewShotPrompt fewShot;

    @Autowired
    private AiServicePromptTemplate promptTemplate;

    @Autowired
    private AiServiceRoleAssignmentPrompt roleAssignment;

    @Autowired
    private AiServiceMultiMessages multiMessages;

    @Autowired
    private AiServiceDefaultMethod defaultMethod;

    @Autowired
    private AiServiceChainOfThoughtPrompt chainOfThought;

    @Autowired
    private AiServiceStepBackPrompt stepBack;

    @Autowired
    private AiServiceSelfConsistency selfConsistency;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Override
    public void run(String... args) throws Exception {
        // Only run when explicitly requested to avoid running on every app start
        if (System.getProperty("runBenchmarks") == null) {
            return;
        }

        int runs = 3;
        String runsProp = System.getProperty("runs");
        if (runsProp != null) {
            try { runs = Integer.parseInt(runsProp); } catch (Exception e) { /* ignore */ }
        }

        System.out.println("Starting prompt benchmarks (runs=" + runs + ")");

        // Sample inputs — keep them identical across techniques where possible
        String review = "이 영화는 배우 연기가 훌륭하지만 스토리가 약합니다.";
        String pizzaOrder = "중간 크기 피자 하나, 페퍼로니와 올리브 추가해 주세요.";
        String translateStatement = "안녕하세요, 반갑습니다.";
        String roleReq = "서울에서 가볼 만한 3곳 추천해주세요.";
        String chatQuestion = "오늘 일정 알려줘";
        String cotQuestion = "동생이 2살일 때 나는 그의 두 배였어. 지금 나는 40살이면 동생은?";
        String stepBackQuestion = "서울에서 울릉도로 여행하려면 어떻게 해야 하나요?";
        String selfConsistencyContent = "이 문장은 중요한 정보가 포함되어 있습니까?";

        // Build a ChatClient instance from the autowired builder for direct calls
        ChatClient chatClient = chatClientBuilder.build();

        Map<String, Supplier<Object>> tasks = new HashMap<>();

        tasks.put("ZeroShot", () -> {
            try {
                return chatClient.prompt()
                    .user("""
                        영화 리뷰를 [긍정적, 중립적, 부정적] 중에서 하나로 분류하세요.
                        레이블만 반환하세요.
                        리뷰: %s
                        """.formatted(review))
                    .options(org.springframework.ai.chat.prompt.ChatOptions.builder().temperature(0.0).maxTokens(8).build())
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("FewShot", () -> {
            try {
                return chatClient.prompt()
                    .user("""
                        고객 주문을 유효한 JSON 형식으로 바꿔주세요.
                        추가 설명은 포함하지 마세요.

                        예시1:
                        작은 피자 하나, 치즈랑 토마토 소스, 페퍼로니 올려서 주세요.
                        JSON 응답:
                        {
                          "size": "small",
                          "type": "normal",
                          "ingredients": ["cheese", "tomato sauce", "pepperoni"]
                        }

                        고객 주문: %s
                        """.formatted(pizzaOrder))
                    .options(org.springframework.ai.chat.prompt.ChatOptions.builder().temperature(0.0).maxTokens(300).build())
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("PromptTemplate", () -> {
            try {
                return chatClient.prompt()
                    .system("답변을 생성할 때 HTML와 CSS를 사용해서 파란 글자로 출력하세요. <span> 태그 안에 들어갈 내용만 출력하세요.")
                    .user("다음 질문을 %s로 답변해주세요. 문장: %s".formatted("English", translateStatement))
                    .stream()
                    .content()
                    .collectList()
                    .block();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("RoleAssignment", () -> {
            try {
                return chatClient.prompt()
                    .system("당신이 여행 가이드 역할을 해 주었으면 좋겠습니다. 아래 요청사항에서 위치를 알려주면, 근처에 있는 3곳을 제안해 주세요.")
                    .user(roleReq)
                    .options(org.springframework.ai.chat.prompt.ChatOptions.builder().temperature(1.0).maxTokens(500).build())
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("MultiMessages", () -> {
            try {
                return chatClient.prompt()
                    .system("당신은 AI 비서입니다. 제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.")
                    .user(chatQuestion)
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("DefaultMethod", () -> {
            try {
                return chatClient.prompt()
                    .user(chatQuestion)
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("ChainOfThought", () -> {
            try {
                return chatClient.prompt()
                    .user(cotQuestion + "\n한 걸음씩 생각해 봅시다.")
                    .stream()
                    .content()
                    .collectList()
                    .block();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("StepBack", () -> {
            try {
                return chatClient.prompt()
                    .user("사용자 질문을 단계별 질문들로 재구성해주세요. JSON 배열로 출력해 주세요. 사용자 질문: %s".formatted(stepBackQuestion))
                    .call()
                    .chatResponse();
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tasks.put("SelfConsistency", () -> {
            // perform multiple calls and return list of ChatResponse
            List<ChatResponse> responses = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
            ChatResponse r = chatClient.prompt()
                .user("다음 내용을 IMPORTANT, NOT_IMPORTANT 둘 중 하나로 분류해 주세요. 내용: %s".formatted(selfConsistencyContent))
                .options(org.springframework.ai.chat.prompt.ChatOptions.builder().temperature(1.0).build())
                .call()
                .chatResponse();
            responses.add(r);
            }
            return responses;
        });

        Map<String, List<Long>> allDurations = new HashMap<>();
        Map<String, String> previews = new HashMap<>();
        Map<String, Object> usages = new HashMap<>();

        for (Map.Entry<String, Supplier<Object>> entry : tasks.entrySet()) {
            String name = entry.getKey();
            Supplier<Object> fn = entry.getValue();
            List<Long> durations = new ArrayList<>();

            System.out.println("\nBenchmarking: " + name);
            String lastPreview = null;
            Object lastUsage = null;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                try {
                    Object out = fn.get();
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    durations.add(durationMs);

                    // try to extract token usage if ChatResponse or list of ChatResponses
                    if (out instanceof ChatResponse) {
                        lastUsage = extractUsage((ChatResponse) out);
                        lastPreview = preview(((ChatResponse) out).getResult().getOutput());
                            } else if (out instanceof List) {
                                List<?> list = (List<?>) out;
                                if (!list.isEmpty() && list.get(0) instanceof ChatResponse) {
                                    List<Object> usageList = new ArrayList<>();
                                    for (Object o : list) {
                                        usageList.add(extractUsage((ChatResponse) o));
                                    }
                                    lastUsage = usageList;
                                    lastPreview = preview(list.get(0));
                                } else {
                                    lastPreview = preview(out);
                                }
                    } else {
                        lastPreview = preview(out);
                    }

                    System.out.println("  run " + (i+1) + ": " + durationMs + " ms, outputPreview=" + lastPreview + (lastUsage != null ? ", usage=" + lastUsage : ""));
                } catch (Exception e) {
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    durations.add(durationMs);
                    System.out.println("  run " + (i+1) + ": ERROR after " + durationMs + " ms: " + e.getMessage());
                }
                // small pause to avoid immediate rate limits
                Thread.sleep(200);
            }

            long sum = 0; for (Long d : durations) sum += d;
            long avg = (sum / durations.size());
            System.out.println("  avg: " + avg + " ms");

            allDurations.put(name, durations);
            previews.put(name, lastPreview == null ? "(null)" : lastPreview);
            if (lastUsage != null) {
                try {
                    Object normalized = normalizeUsage(lastUsage);
                    usages.put(name, normalized == null ? lastUsage.toString() : normalized);
                } catch (Exception ignored) {
                    usages.put(name, lastUsage.toString());
                }
            }
        }

        System.out.println("\nBenchmarks finished.");

        // If output file requested, write JSON summary
        String outFile = System.getProperty("outputFile");
        if (outFile != null && !outFile.isBlank()) {
            Path p = Paths.get(outFile);
            try {
                Files.createDirectories(p.getParent() == null ? Paths.get(".") : p.getParent());
            } catch (Exception ignored) {}

            Map<String, Object> report = new HashMap<>();
            report.put("runs", System.getProperty("runs", "3"));
            report.put("durations", allDurations);
            report.put("previews", previews);
            report.put("usage", usages);

            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(p.toFile(), report);
                System.out.println("Wrote benchmark report to: " + p.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to write benchmark report: " + e.getMessage());
            }
        }
    }

    private String preview(Object out) {
        if (out == null) return "(null)";
        String s = out.toString().replaceAll("\n", " ");
        if (s.length() > 120) return s.substring(0, 120) + "...";
        return s;
    }

    private Object extractUsage(ChatResponse resp) {
        if (resp == null) return null;
        try {
            Method getResult = resp.getClass().getMethod("getResult");
            Object result = getResult.invoke(resp);
            if (result != null) {
                try {
                    Method getUsage = result.getClass().getMethod("getUsage");
                    Object usage = getUsage.invoke(result);
                    if (usage != null) return usage;
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}

        try {
            Method getUsageDirect = resp.getClass().getMethod("getUsage");
            Object usage = getUsageDirect.invoke(resp);
            if (usage != null) return usage;
        } catch (Exception ignored) {}

        return null;
    }

    private Map<String, Integer> normalizeUsage(Object usage) {
        if (usage == null) return null;

        Map<String, Integer> result = new HashMap<>();

        // candidate field/method names to look for
        String[] candidates = new String[] {
            "prompt_tokens", "completion_tokens", "total_tokens",
            "promptTokens", "completionTokens", "totalTokens",
            "promptTokenCount", "completionTokenCount", "totalTokenCount",
            "promptToken", "completionToken", "totalToken"
        };

        // If usage is a map-like object
        if (usage instanceof Map) {
            Map<?,?> m = (Map<?,?>) usage;
            for (String name : candidates) {
                for (Object key : m.keySet()) {
                    if (key == null) continue;
                    String ks = key.toString();
                    if (ks.equalsIgnoreCase(name) || ks.replaceAll("[^A-Za-z]","" ).equalsIgnoreCase(name.replaceAll("[^A-Za-z]",""))) {
                        Object v = m.get(key);
                        Integer iv = asInteger(v);
                        if (iv != null) result.put(normalizeKey(name), iv);
                    }
                }
            }
            return result.isEmpty() ? null : result;
        }

        // Otherwise try methods/fields via reflection
        for (String name : candidates) {
            // try getter methods
            String[] methodNames = new String[] {
                "get" + capitalize(name), name, "get" + removeNonAlphaAndCap(name)
            };
            for (String mname : methodNames) {
                try {
                    Method m = usage.getClass().getMethod(mname);
                    Object v = m.invoke(usage);
                    Integer iv = asInteger(v);
                    if (iv != null) {
                        result.put(normalizeKey(name), iv);
                        break;
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }
            }
        }

        return result.isEmpty() ? null : result;
    }

    private Integer asInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private String normalizeKey(String k) {
        if (k == null) return k;
        // replace non-alphanumeric with underscore
        String cleaned = k.replaceAll("[^A-Za-z0-9]", "_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        String out = sb.toString().replaceAll("__+", "_").toLowerCase();
        if (out.startsWith("_")) out = out.substring(1);
        if (out.endsWith("_")) out = out.substring(0, out.length()-1);
        return out;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.contains("_")) {
            String[] parts = s.split("_");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) sb.append(p.substring(0,1).toUpperCase()).append(p.substring(1));
            return sb.toString();
        }
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String removeNonAlphaAndCap(String s) {
        String cleaned = s.replaceAll("[^A-Za-z0-9]", "");
        return capitalize(cleaned);
    }
}
