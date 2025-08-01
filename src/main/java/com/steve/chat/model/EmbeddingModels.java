package com.steve.chat.model;


import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import io.helidon.config.Config;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class EmbeddingModels {

  private static final String OLLAMA_URL = Config.global().get("agent.ollama.url").asString().get();
  private static final String OLLAMA_EMBEDDING_MODEL_NAME = Config.global().get("agent.ollama.embedding_model_name").asString().get();

  public static final EmbeddingModel OLLAMA_EMBEDDING_MODEL = OllamaEmbeddingModel.builder()
                                                                  .baseUrl(OLLAMA_URL)
                                                                  .modelName(OLLAMA_EMBEDDING_MODEL_NAME)
                                                                  .logRequests(false)
                                                                  .timeout(Duration.of(10, ChronoUnit.MINUTES))
                                                                  .build();
}
