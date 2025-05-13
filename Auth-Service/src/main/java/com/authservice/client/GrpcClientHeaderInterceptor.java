package com.authservice.client;

import io.grpc.*;

public class GrpcClientHeaderInterceptor implements ClientInterceptor {

    private final String headerName;
    private final String headerValue;

    public GrpcClientHeaderInterceptor(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Metadata.Key<String> customHeaderKey = Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
                headers.put(customHeaderKey, headerValue);
                super.start(responseListener, headers);
            }
        };
    }
}

