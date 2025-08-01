package com.steve.chat.assistant;

import dev.langchain4j.service.TokenStream;
import io.helidon.websocket.WsSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AssistantService {

  private static final Log LOGGER = LogFactory.getLog(AssistantService.class);

  public static void generateStreamingResponse(WsSession session, TokenStream chat) {
    chat.onPartialResponse(token -> session.send(token, false))
        .onRetrieved(token -> {
        })
        .onToolExecuted(System.out::println)
        .onCompleteResponse(response -> {
          LOGGER.info(String.format("Chat response for room: %s sent.", session.socketContext().childSocketId()));
          session.send("", true);
        })
        .onError(error -> {
          session.send(error.getMessage(), true);
          error.printStackTrace();
        })
        .start();
  }

  public static void generateStandardResponse(WsSession session, String response) {
    LOGGER.info(String.format("Chat response for room: %s sent.", session.socketContext().childSocketId()));
    session.send(response, true);
  }
}
