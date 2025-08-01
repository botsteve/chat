package com.steve.chat;


import com.steve.chat.embedding.OpensearchEmbeddingStoreService;
import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.websocket.WsRouting;
import org.slf4j.bridge.SLF4JBridgeHandler;


/**
 * The application main class.
 */
public class Main {


  /**
   * Cannot be instantiated.
   */
  private Main() {
  }

  public static void postServerStart() {
    OpensearchEmbeddingStoreService.embedRAGDirectory();
  }

  /**
   * Application main entry point.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    // load logging configuration
    LogConfig.configureRuntime();

    // initialize global config from default configuration
    Config config = Config.create();
    Config.global(config);


    var builder = WebServer.builder();
    builder.config(config.get("server"))
        .routing(Main::routing);


    builder.addRouting(WsRouting.builder()
                           .endpoint("/chat", ChatEndpoint::new));

    var server = builder.build().start();
    postServerStart();
    System.out.println("WEB server is up! http://localhost:" + server.port());
  }

  /**
   * Updates HTTP Routing.
   */
  static void routing(HttpRouting.Builder routing) {
    routing.register("/", CorsSupport.create());
  }
}