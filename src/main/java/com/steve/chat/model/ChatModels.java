package com.steve.chat.model;


import static com.steve.chat.Constants.COMPARTMENT_ID;
import static com.steve.chat.Constants.MODEL_ID;

import dev.langchain4j.model.localai.LocalAiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import io.helidon.config.Config;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ChatModels {

  public static final LocalAiStreamingChatModel LOCAL_AI_CHAT_MODEL = LocalAiStreamingChatModel.builder()
                                                                          .baseUrl("http://localhost:8181/v1")
                                                                          .modelName("hermes-2-theta-llama-3-8b")
                                                                          .temperature(0.0)
                                                                          .topP(1.0)
                                                                          .build();
  public static final OCIGenAiChatLanguageModel OCI_GEN_AI_MODEL = new OCIGenAiChatLanguageModel(COMPARTMENT_ID, MODEL_ID);
  public static final OllamaStreamingChatModel OLLAMA_STREAMING_CHAT_MODEL = OllamaStreamingChatModel.builder()
                                                                                 .baseUrl(Config.global()
                                                                                              .get("agent.ollama.url")
                                                                                              .asString()
                                                                                              .get())
                                                                                 .modelName(Config.global()
                                                                                                .get(
                                                                                                    "agent.ollama.llm_model_name")
                                                                                                .asString()
                                                                                                .get())
                                                                                 .temperature(0.0)
                                                                                 .topP(1.0)
                                                                                 .timeout(Duration.of(5, ChronoUnit.MINUTES))
                                                                                 .build();
}
