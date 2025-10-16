package com.recipientservice.aop;

import com.recipientservice.exceptions.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class InternalOnlyAspect {

    private final HttpServletRequest request;

    @Value("${internal.access-token}")
    private String internalAccessToken;

    @Pointcut("@annotation(com.recipientservice.aop.InternalOnly)")
    public void internalOnlyMethods() {}

    @Before("internalOnlyMethods()")
    public void validateInternalAccess() {
        String token = request.getHeader("Internal-Access-Token");

        if (token == null || !token.equals(internalAccessToken)) {
            throw new AccessDeniedException("Access denied: internal use only.");
        }
    }
}

