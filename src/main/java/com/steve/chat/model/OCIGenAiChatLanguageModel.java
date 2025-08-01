package com.steve.chat.model;

import static com.steve.chat.Constants.CONFIG_LOCATION;
import static com.steve.chat.Constants.CONFIG_PROFILE;
import static com.steve.chat.Constants.ENDPOINT;
import static com.steve.chat.Constants.REGION;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.CohereChatBotMessage;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.CohereResponseFormat;
import com.oracle.bmc.generativeaiinference.model.CohereResponseJsonFormat;
import com.oracle.bmc.generativeaiinference.model.CohereResponseTextFormat;
import com.oracle.bmc.generativeaiinference.model.CohereSystemMessage;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessage;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.retrier.RetryConfiguration;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.output.Response;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class OCIGenAiChatLanguageModel implements ChatLanguageModel {

  private static final Predicate<ChatMessage> isUserMessage = UserMessage.class::isInstance;
  private static final ConfigFileReader.ConfigFile configFile;
  private static final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                                                                     .readTimeoutMillis(240000)
                                                                     .retryConfiguration(
                                                                         RetryConfiguration.NO_RETRY_CONFIGURATION)
                                                                     .build();

  static {
    try {
      configFile = ConfigFileReader.parse(CONFIG_LOCATION, CONFIG_PROFILE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
  private final GenerativeAiInferenceClient client;
  private final String compartmentId;
  private final String modelId;

  public OCIGenAiChatLanguageModel(String compartmentId, String modelId) {
    this.compartmentId = compartmentId;
    this.modelId = modelId;
    this.client = GenerativeAiInferenceClient.builder()
                      .configuration(clientConfiguration)
                      .region(REGION)
                      .endpoint(ENDPOINT)
                      .build(provider);
  }


  @Override
  public Response<AiMessage> generate(List<ChatMessage> messages) {
    return doGenerate(messages, null, ResponseFormat.JSON);
  }

  public List<CohereMessage> convertChatMessagesToCohere(List<ChatMessage> messages) {
    return messages.stream()
               .map(message -> {
                 if (message instanceof SystemMessage msg) {
                   return CohereSystemMessage.builder().message(msg.text()).build();
                 } else if (message instanceof AiMessage msg) {
                   return CohereChatBotMessage.builder().message(msg.text()).build();
                 } else {
                   return CohereUserMessage.builder().message(((UserMessage) message).singleText()).build();
                 }
               })
               .toList();
  }

  public CohereResponseFormat getResponseFormat(ResponseFormat responseFormat) {
    if (responseFormat == ResponseFormat.JSON) {
      return CohereResponseJsonFormat.builder().build();
    } else {
      return CohereResponseTextFormat.builder().build();
    }
  }

  private Response<AiMessage> doGenerate(List<ChatMessage> messages,
                                         List<ToolSpecification> toolSpecifications,
                                         ResponseFormat responseFormat) {

    var lastUserMessage = messages.stream()
                              .filter(isUserMessage)
                              .map(UserMessage.class::cast)
                              .toList()
                              .getLast();

    var cohereMessages = convertChatMessagesToCohere(messages);

    CohereChatRequest chatRequest = CohereChatRequest.builder()
                                        .message(lastUserMessage.singleText())
                                        .chatHistory(cohereMessages)
                                        .maxTokens(600)
                                        .temperature(1.0D)
                                        .frequencyPenalty(0.0D)
                                        .topP(0.75D)
                                        .topK(0)
                                        .seed(null)
                                        .isStream(false)
                                        .responseFormat(getResponseFormat(responseFormat))
                                        .tools(null)
                                        .build();
    ChatDetails details = ChatDetails.builder()
                              .servingMode(OnDemandServingMode.builder()
                                               .modelId(modelId)
                                               .build())
                              .compartmentId(compartmentId)
                              .chatRequest(chatRequest)
                              .build();
    ChatRequest request = ChatRequest.builder()
                              .chatDetails(details)
                              .build();
    ChatResponse response = client.chat(request);
    return Response.from(AiMessage.from(response.toString()));
  }
}
