package com.joebrothers;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joebrothers.thrift.HelloRequest;
import com.joebrothers.thrift.HelloService.blockingHello_args;
import com.joebrothers.thrift.HelloService.hello_args;
import com.joebrothers.thrift.HelloService.lazyHello_args;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.thrift.THttpService;

public final class RelayServer {

    private static final Logger logger = LoggerFactory.getLogger(RelayServer.class);

    public static void main(String[] args) {
        final Server server = newServer(8080, 8443);
        server.closeOnJvmShutdown();

        server.start().join();

        logger.info("Server started / DocService at http://127.0.0.1:{}/docs", server.activeLocalPort());
    }

    private RelayServer() {}

    static Server newServer(int httpPort, int httpsPort) {
        final ServerBuilder sb = Server.builder();
        sb.http(httpPort)
          .https(httpsPort)
          .tlsSelfSigned();
        configureServices(sb);
        return sb.build();
    }

    static void configureServices(ServerBuilder sb) {
        final HelloRequest exampleRequest = new HelloRequest("Armeria");
        final THttpService thriftService =
                THttpService.builder()
                            .addService(new HelloServiceImpl())
                            .build();
        sb.service("/", thriftService)
          .service("/second", thriftService)
          // You can access the documentation service at http://127.0.0.1:8080/docs.
          // See https://armeria.dev/docs/server-docservice for more information.
          .serviceUnder("/docs",
                        DocService.builder()
                                  .exampleRequests(Arrays.asList(
                                          new hello_args(exampleRequest),
                                          new lazyHello_args(exampleRequest),
                                          new blockingHello_args(exampleRequest)))
                                  .build());
    }
}
