package com.steve.chat.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface Assistant {

  String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}