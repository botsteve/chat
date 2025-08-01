package com.steve.chat;

import static com.steve.chat.assistant.AssistantFactory.getOrCreateAssistant;
import static com.steve.chat.assistant.AssistantFactory.getOrCreateStreamingAssistant;
import static com.steve.chat.assistant.AssistantService.generateStandardResponse;
import static com.steve.chat.assistant.AssistantService.generateStreamingResponse;

import io.helidon.websocket.WsSession;

public class ChatService {


  public static void sentStreamingResponse(String message, WsSession session) {
    var assistant = getOrCreateStreamingAssistant(session.socketContext().childSocketId());
    var chat = assistant.chat(session.socketContext().childSocketId(), message);
    generateStreamingResponse(session, chat);
  }

  public static void sentStandardResponse(String message, WsSession session) {
    var assistant = getOrCreateAssistant(session.socketContext().childSocketId());
    var chat = assistant.chat(session.socketContext().childSocketId(), message);
    generateStandardResponse(session, chat);
  }
}
