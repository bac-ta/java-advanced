package com.example.javaadvanced.reactorcontext.context;

import reactor.util.context.ContextView;


public record RequestContextView(String userId, String tenantId, String traceId) {

    public static RequestContextView from(ContextView ctx) {
        return new RequestContextView(
                ctx.getOrDefault(RequestContextKeys.USER_ID, "anonymous"),
                ctx.getOrDefault(RequestContextKeys.TENANT_ID, null),
                ctx.getOrDefault(RequestContextKeys.TRACE_ID, "N/A")
        );
    }
}
