package com.rickrone.logiflow.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiChatController {
  private final ChatClient chatClient;

  public AiChatController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
            .defaultSystem("你是 LogiFlow 系统的智能助手，擅长解答物资仓储、订单流转与技术架构问题，回答简洁清晰。").build();
  }

  /**
   * SSE 流式对话接口
   * GET /api/ai/chat/stream?message=你好
   */
  @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> streamChat(@RequestParam String message) {
    log.info("收到流式对话请求: {}", message);
    return chatClient.prompt()
            .user(message)
            .stream()
            .content()
            .onErrorResume(ex -> {
              log.error("AI 流式输出异常", ex);
              return Flux.just("[服务暂时开小差，请稍后重试]");
            });
  }
}
