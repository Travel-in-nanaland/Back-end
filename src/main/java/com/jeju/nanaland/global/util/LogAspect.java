package com.jeju.nanaland.global.util;

import com.jeju.nanaland.domain.member.dto.MemberResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
public class LogAspect {

  @Pointcut(
      "execution(* com.jeju.nanaland..*.*(..))")
  public void all() {
  }

  @Pointcut("execution(* com.jeju.nanaland..*Controller*.*(..))")
  public void controllerPointcut() {
  }

  @Pointcut("execution(* com.jeju.nanaland..*Service*.*(..))")
  public void servicePointcut() {
  }

  @Pointcut("execution(* com.jeju.nanaland..*Repository*.*(..))")
  public void repositoryPointcut() {
  }

  // 메서드 호출 전: HTTP 메서드, 함수명, 파라미터 출력 및 시간 측정 시작
  @Before("controllerPointcut()")
  public void logBeforeController(JoinPoint joinPoint) {

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null || isExceptionHandlerMethod(joinPoint)) {
      // 요청이 없거나 예외가 발생한 경우, 로그를 남기지 않음
      return;
    }
    HttpServletRequest request = attributes.getRequest();

    StringBuilder logBuilder = new StringBuilder();
    String httpMethod = request.getMethod();
    String methodName = joinPoint.getSignature().getName();
    String params = Arrays.toString(joinPoint.getArgs());

    long nowTime = System.currentTimeMillis();
    request.setAttribute("startTime", nowTime);
    request.setAttribute("logBuilder", logBuilder);

    containUserInfoInLogBuilder(joinPoint, logBuilder);
    logBuilder.append("HTTP Method: ").append(httpMethod)
        .append(" / Controller Method: ").append(methodName)
        .append(" / Parameters: ").append(params).append("\n");
  }

  // 서비스 및 레포지토리 메서드의 개별 실행 시간 측정
  @Around("repositoryPointcut()||servicePointcut()")
  public Object logServiceAndRepositoryExecutionTime(ProceedingJoinPoint joinPoint)
      throws Throwable {
    // 요청이 있는 경우에만 실행 시간 측정
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    StringBuilder logBuilder = null;
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      logBuilder = (StringBuilder) request.getAttribute("logBuilder");
    }

    long start = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      long executionTime = System.currentTimeMillis() - start;
      if (logBuilder != null) {
        logBuilder.append("Execution time of ").append(joinPoint.getSignature())
            .append(" : ").append(executionTime).append(" ms\n");
      }
    }
  }

  // 메서드 실행 후 총 시간 계산
  @AfterReturning("controllerPointcut()")
  public void logAfterController(JoinPoint joinPoint) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    if (request.getAttribute("startTime") == null) {
      return;
    }

    long startTime = (Long) request.getAttribute("startTime");
    long totalTime = System.currentTimeMillis() - startTime;

    StringBuilder logBuilder = (StringBuilder) request.getAttribute("logBuilder");
    if (logBuilder != null && !isExceptionHandlerMethod(joinPoint)) {
      logBuilder.append("Total execution time of controller method ")
          .append(joinPoint.getSignature().getName()).append(" : ")
          .append(totalTime).append(" ms");

      // 최종 로그 한 번에 출력
      log.info(logBuilder.toString());

    }
  }

  @AfterThrowing(pointcut = "all() && !controllerPointcut()", throwing = "exception")
  public void logException(JoinPoint joinPoint, Throwable exception) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    StringBuilder logBuilder = (StringBuilder) request.getAttribute("logBuilder");
    // 예외가 발생한 메서드명
    String signature = String.valueOf(joinPoint.getSignature());

    // 로깅
    if (logBuilder != null) {
      log.error(logBuilder.toString());
    }
    log.error("Exception Occurred From : {}, Exception Message : {}",
        signature, exception.toString());
  }

  private boolean isExceptionHandlerMethod(JoinPoint joinPoint) {
    // 예외 처리 메서드인지 확인
    String methodName = joinPoint.getSignature().getName();
    return methodName.contains("Exception");
  }

  private void containUserInfoInLogBuilder(JoinPoint joinPoint, StringBuilder logBuilder) {
    Object[] args = joinPoint.getArgs();
    for (Object arg : args) {
      if (arg instanceof MemberResponse.MemberInfoDto memberInfoDto) { // memberInfoDto 타입 확인
        logBuilder.append("Member ID: ").append(memberInfoDto.getMember().getId())
            .append("\n"); // ID 추가
        break; // 찾으면 반복문 종료
      }
    }
  }
//  // 컨트롤러 메서드 호출 시 개별 메서드 실행 시간 측정
//  @Around("controllerPointcut()")
//  public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
//    long start = System.currentTimeMillis();
//    try {
//      return joinPoint.proceed();
//    } finally {
//      long executionTime = System.currentTimeMillis() - start;
//      log.info("Execution time of Controller {} : {} ms", joinPoint.getSignature(), executionTime);
//    }
//  }

//  @Around("all()")
//  public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {
//    long start = System.currentTimeMillis();
//    try {
//      Object result = joinPoint.proceed();
//      return result;
//    } finally {
//      long end = System.currentTimeMillis();
//      long timeinMs = end - start;
//      log.info("!!!{} | time = {}ms", joinPoint.getSignature(), timeinMs);
//    }
//  }
//
//  @Around("controllerPointcut()")
//  public Object logging2(ProceedingJoinPoint joinPoint) throws Throwable {
//    long start = System.currentTimeMillis();
//    try {
//      Object result = joinPoint.proceed();
//      return result;
//    } finally {
//      long end = System.currentTimeMillis();
//      long timeinMs = end - start;
//      log.info("????{} | time = {}ms", joinPoint.getSignature(), timeinMs);
//    }
//  }

//  @Before("controllerPointcut()")
//  public void before(JoinPoint joinPoint) {
//    // HTTP 요청 정보를 가져오기 위해 사용
//    HttpServletRequest request =
//        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//
//    // HTTP 메서드(GET, POST, 등)
//    String httpMethod = request.getMethod();
//
//    // 호출된 메서드명
//    String methodName = joinPoint.getSignature().getName();
//
//    // 파라미터들
//    Object[] args = joinPoint.getArgs();
//    String params = Arrays.toString(args);
//
//    // 로깅
//    log.info("-----------------");
//    log.info("HTTP Method: {}", httpMethod);
//    log.info("Before Method Execution: {}", methodName);
//    log.info("Parameters: {}", params);
//    log.info("-----------------");
//  }

  //  @AfterReturning("controllerPointcut()")
//  public void afterReturning(JoinPoint joinPoint) {
//  }
//

//  @After("controllerPointcut()")
//  public void after(JoinPoint joinPoint) {
//  }
  //  @Around 메서드가 빈 상태로 있거나 proceed()를 호출하지 않으면 컨트롤러가 응답을 반환하지 않게 됩니다.
//  @Around("controllerPointcut()")
//  public void around(ProceedingJoinPoint joinPoint) throws Throwable {
//  }
}

