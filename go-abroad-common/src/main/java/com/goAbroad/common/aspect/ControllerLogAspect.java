package com.goAbroad.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * Controller 日志切面
 * 统一打印所有接口的请求参数和响应结果
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLogAspect {

    private final ObjectMapper objectMapper;

    /**
     * 切入点：所有 Controller 类的所有方法
     */
    @Pointcut("execution(* com.goAbroad..controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 请求前打印入参
     */
    @Before("controllerPointcut()")
    public void logRequestParams(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取请求参数
        String requestParams = getRequestParams(joinPoint);

        // 打印请求日志
        log.info("======> Request: {} {} | Params: {}",
                request.getMethod(),
                request.getRequestURI(),
                requestParams);
    }

    /**
     * 响应后打印返回结果
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logResponseResult(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {return;}
        HttpServletRequest request = attributes.getRequest();

        // 1. 【优先判断】如果是流，打印完日志直接收工，不进行后续任何处理
        if (result instanceof org.springframework.web.servlet.mvc.method.annotation.SseEmitter) {
            log.info("======> Response: {} {} | Result: [SSE Stream Started]",
                    request.getMethod(), request.getRequestURI());
            return;
        }

        // 打印响应日志
        log.info("======> Response: {} {} | Result: {}",
                request.getMethod(),
                request.getRequestURI(),
                truncateResult(result));
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "{}";
        }

        try {
            // 过滤掉不可序列化的参数（如 HttpServletRequest, HttpServletResponse 等）
            StringBuilder sb = new StringBuilder();
            sb.append("[");

            boolean first = true;
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }

                // 跳过 servlet 相关对象
                String className = arg.getClass().getName();
                if (className.contains("HttpServletRequest") ||
                    className.contains("HttpServletResponse") ||
                    className.contains("ServletRequest") ||
                    className.contains("ServletResponse")) {
                    continue;
                }

                if (!first) {
                    sb.append(", ");
                }
                first = false;

                sb.append(objectMapper.writeValueAsString(arg));
            }

            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    /**
     * 截断过长的返回结果
     */
    private String truncateResult(Object result) {
        if (result == null) {
            return "null";
        }

        try {
            String json = objectMapper.writeValueAsString(result);
            // 如果结果过长，截断显示
            if (json.length() > 500) {
                return json.substring(0, 500) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return result.toString();
        }
    }
}
