package com.steve.chat.model;

import static com.steve.chat.embedding.OpensearchEmbeddingStoreService.OPENSEARCH_EMBEDDING_STORE;
import static com.steve.chat.model.EmbeddingModels.OLLAMA_EMBEDDING_MODEL;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;

public class ContentRetrievers {

  public static final ContentRetriever EMBEDDING_CONTENT_RETRIEVER = EmbeddingStoreContentRetriever.builder()
                                                                         .embeddingStore(OPENSEARCH_EMBEDDING_STORE)
                                                                         .embeddingModel(OLLAMA_EMBEDDING_MODEL)
                                                                         .maxResults(75)
                                                                         .minScore(0.70)
                                                                         .build();
}
