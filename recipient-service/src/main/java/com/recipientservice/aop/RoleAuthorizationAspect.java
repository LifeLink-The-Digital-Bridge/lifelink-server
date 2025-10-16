package com.recipientservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Around("@annotation(requireRole)")
    public Object authorizeByRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();
        String rolesHeader = request.getHeader("roles");

        if (rolesHeader == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: No roles found.");
        }

        List<String> userRoles = Arrays.asList(rolesHeader.split(","));

        String[] requiredRoles = requireRole.value();

        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(requiredRole -> userRoles.stream()
                        .anyMatch(userRole -> userRole.trim().equalsIgnoreCase(requiredRole.trim())));

        if (!hasRequiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: One of these roles required: " + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }
}
