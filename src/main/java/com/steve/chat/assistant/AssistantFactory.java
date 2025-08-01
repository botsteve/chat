package com.steve.chat.assistant;

import static com.steve.chat.model.ChatModels.OCI_GEN_AI_MODEL;
import static com.steve.chat.model.ChatModels.OLLAMA_STREAMING_CHAT_MODEL;
import static com.steve.chat.model.ContentRetrievers.EMBEDDING_CONTENT_RETRIEVER;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssistantFactory {

  private static final Map<String, StreamingAssistant> STREAMING_ASSISTANT_SESSIONS = new ConcurrentHashMap<>();
  private static final Map<String, Assistant> ASSISTANT_SESSIONS = new ConcurrentHashMap<>();

  public static StreamingAssistant getOrCreateStreamingAssistant(String id) {
    return STREAMING_ASSISTANT_SESSIONS.computeIfAbsent(id, o -> createStreamingAssistant());
  }

  public static Assistant getOrCreateAssistant(String id) {
    return ASSISTANT_SESSIONS.computeIfAbsent(id, o -> createAssistant());
  }


  private static StreamingAssistant createStreamingAssistant() {
    QueryRouter queryRouter = new DefaultQueryRouter(EMBEDDING_CONTENT_RETRIEVER);
    RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                                                .queryRouter(queryRouter)
                                                .build();
    return AiServices.builder(StreamingAssistant.class)
               .streamingChatLanguageModel(OLLAMA_STREAMING_CHAT_MODEL)
               .retrievalAugmentor(retrievalAugmentor)
               .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
               .build();
  }

  private static Assistant createAssistant() {
    QueryRouter queryRouter = new DefaultQueryRouter(EMBEDDING_CONTENT_RETRIEVER);
    RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                                                .queryRouter(queryRouter)
                                                .build();
    return AiServices.builder(Assistant.class)
               .chatLanguageModel(OCI_GEN_AI_MODEL)
//               .retrievalAugmentor(retrievalAugmentor)
               .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
               .build();
  }
}
