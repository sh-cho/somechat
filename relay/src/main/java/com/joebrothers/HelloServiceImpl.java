package com.joebrothers;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import com.joebrothers.thrift.HelloReply;
import com.joebrothers.thrift.HelloRequest;
import com.joebrothers.thrift.HelloService;

import com.linecorp.armeria.server.ServiceRequestContext;

class HelloServiceImpl implements HelloService.AsyncIface {

    @Override
    public void hello(HelloRequest request, AsyncMethodCallback resultHandler) throws TException {
        // Make sure that current thread is request context aware
        ServiceRequestContext.current();
        resultHandler.onComplete(toMessage(request.getName()));
    }

    @Override
    public void lazyHello(HelloRequest request, AsyncMethodCallback resultHandler) throws TException {
        ServiceRequestContext.current().eventLoop().schedule(() -> {
            resultHandler.onComplete(buildReply(toMessage(request.getName())));
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    public void blockingHello(HelloRequest request, AsyncMethodCallback resultHandler) throws TException {
        ServiceRequestContext.current().blockingTaskExecutor().execute(() -> {
            try {
                // Simulate a blocking API call.
                Thread.sleep(3000);
            } catch (Exception ignored) {
                // Do nothing.
            }
            resultHandler.onComplete(buildReply(toMessage(request.getName())));
        });
    }

    private static String toMessage(String name) {
        return "Hello, " + name + '!';
    }

    private static HelloReply buildReply(String message) {
        return new HelloReply(message);
    }
}
