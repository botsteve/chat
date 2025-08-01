package com.steve.chat;

import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ChatEndpoint implements WsListener {

  private static final Logger LOGGER = Logger.getLogger(ChatEndpoint.class.getSimpleName());
  private static final Map<String, WsSession> CHAT_ROOMS = new ConcurrentHashMap<>();

  @Override
  public void onMessage(WsSession session, String text, boolean last) {
//    ChatService.sentStreamingResponse(text, session);
    ChatService.sentStandardResponse(text, session);
    System.out.println("Received message in chat room " + session.socketContext().childSocketId() + ": " + text);
  }

  @Override
  public void onClose(WsSession session, int status, String reason) {
    LOGGER.info("WebSocket closed for " + session.hashCode() + " with reason " + reason);
    CHAT_ROOMS.remove(session.socketContext().childSocketId());
  }

  @Override
  public void onError(WsSession session, Throwable t) {
    LOGGER.info("WebSocket error for " + session.socketContext().childSocketId() + " " + t.getMessage());
    session.send(t.getMessage(), true);
  }

  @Override
  public void onOpen(WsSession session) {
    CHAT_ROOMS.put(session.socketContext().childSocketId(), session);
    LOGGER.info("WebSocket connection opened in chat room: " + session.socketContext().childSocketId());
  }
}