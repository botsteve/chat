package com.steve.chat.embedding;


import static com.steve.chat.model.EmbeddingModels.OLLAMA_EMBEDDING_MODEL;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import io.helidon.config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpensearchEmbeddingStoreService {

  private static final String OPENSEARCH_URL = Config.global().get("agent.opensearch.url").asString().get();
  private static final String OPENSEARCH_OLLAMA_INDEX_NAME = Config.global().get("agent.opensearch.ollama_index_name").asString().get();
  private static final boolean RUN_EMBEDDING = Config.global().get("agent.rag.run_embedding").asBoolean().get();
  private static final String RAG_SOURCE = Config.global().get("agent.rag.source").asString().get();

  public static final EmbeddingStore<TextSegment> OPENSEARCH_EMBEDDING_STORE = OpenSearchEmbeddingStore.builder()
                                                                                   .serverUrl(OPENSEARCH_URL)
                                                                                   .indexName(OPENSEARCH_OLLAMA_INDEX_NAME)
                                                                                   .build();
  private static final Logger LOGGER = Logger.getLogger(OpensearchEmbeddingStoreService.class.getName());


  public static void embedRAGDirectory() {
    if (RUN_EMBEDDING) {
      LOGGER.info("Starting embedding docs...");
      var path = Path.of(RAG_SOURCE);
      try (var files = Files.list(path)) {
        files.forEach(file -> {
          embed(file.toAbsolutePath());
        });
      } catch (IOException e) {
        LOGGER.log(Level.INFO, e.getMessage(), e);
      }
      LOGGER.info("Finished embedding docs...");
    }
  }

  public static void embed(Path documentPath) {
    Document document;
    if (documentPath.endsWith(".json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      String openApiText;
      try {
        var jsonNode = objectMapper.readTree(documentPath.toFile());
        openApiText = jsonNode.toPrettyString().trim().replaceAll("\\s+", "");
        document = Document.document(openApiText);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      document = FileSystemDocumentLoader.loadDocument(documentPath);
    }
    var splitter = DocumentSplitters.recursive(200, 20);
    var segments = splitter.split(document);
    LOGGER.info("Calculate embeddings for " + documentPath.getFileName().toString());
    var embeddings = OLLAMA_EMBEDDING_MODEL.embedAll(segments).content();
    LOGGER.info(String.format("Loading %d embeddings and %d segments for %s...",
                              embeddings.size(),
                              segments.size(),
                              documentPath.getFileName().toString()));
    var lists1 = splitList(segments, 200);
    var lists = splitList(embeddings, 200);
    for (int i = 0; i < lists1.size(); i++) {
      OPENSEARCH_EMBEDDING_STORE.addAll(lists.get(i), lists1.get(i));
    }
  }

  public static void embed(String text) {
    TextSegment segment1 = TextSegment.from(text);
    Embedding embedding1 = OLLAMA_EMBEDDING_MODEL.embed(segment1).content();
    OPENSEARCH_EMBEDDING_STORE.add(embedding1, segment1);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static String search(String query) {
    var queryEmbedding = OLLAMA_EMBEDDING_MODEL.embed(query).content();
    var build = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).build();
    var searchResult = OPENSEARCH_EMBEDDING_STORE.search(build);
    var embeddingMatch = searchResult.matches().getFirst();

    System.out.println(embeddingMatch.score());
    System.out.println(embeddingMatch.embedded().text());
    return embeddingMatch.embedded().text();
  }

  public static <T> List<List<T>> splitList(List<T> originalList, int chunkSize) {
    List<List<T>> chunks = new ArrayList<>();
    for (int i = 0; i < originalList.size(); i += chunkSize) {
      chunks.add(new ArrayList<>(originalList.subList(i, Math.min(i + chunkSize, originalList.size()))));
    }
    return chunks;
  }
}
