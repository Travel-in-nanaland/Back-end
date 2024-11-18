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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
public class LogAspect {

  // 모든 파일
  @Pointcut(
      "execution(* com.jeju.nanaland..*.*(..))")
  public void all() {
  }

  // 컨트롤러
  @Pointcut("execution(* com.jeju.nanaland..*Controller*.*(..))")
  public void controllerPointcut() {
  }

  // 서비스
  @Pointcut("execution(* com.jeju.nanaland..*Service*.*(..))")
  public void servicePointcut() {
  }

  // 레포지토리
  @Pointcut("execution(* com.jeju.nanaland..*Repository*.*(..))")
  public void repositoryPointcut() {
  }

  // 컨트롤러단에서 로그 찍기.
  // 모든 메서드 (레포,서비스에 있는)가 단일 로그로 찍히게 되면 동시 요청이 있을 때 사용자 별 로그가 겹침
  // request에 stringBuilder를 포함시켜서 로그 저장함
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

  // 서비스 및 레포지토리 메서드의 개별 실행 시간 측정해서 stringbuilder에 저장
  @Around("repositoryPointcut()||servicePointcut()")
  public Object logServiceAndRepositoryExecutionTime(ProceedingJoinPoint joinPoint)
      throws Throwable {

    // 요청이 있는 경우에만 실행 시간 측정
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    StringBuilder logBuilder = null;
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      logBuilder = (StringBuilder) request.getAttribute("logBuilder");
    } else { // 요청 없는 경우 바로진행 -> 스케줄러 예외 처
      return joinPoint.proceed();
    }

    long start = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      long executionTime = System.currentTimeMillis() - start;
      if (logBuilder != null) {
        logBuilder.append("\nExecution time of ").append(joinPoint.getSignature())
            .append(" : ").append(executionTime).append(" ms\n");
      }
    }
  }

  // 정상적으로 controller에서 return이 되면
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
      logBuilder.append("\nTotal execution time of controller method ")
          .append(joinPoint.getSignature().getName()).append(" : ")
          .append(totalTime).append(" ms\n");

      // 최종 로그 한 번에 출력
      log.info(logBuilder.toString());

    }
  }

  // 에러 발생 시
  // 컨트롤러 단도 포함하면 에러 로그가 두번 찍힘
  // 에러 발생 (로그 한번) -> controllerAdvice에 의해 에러 값 return(로그 한번 더 찍힘)
  @AfterThrowing(pointcut = "all() && !controllerPointcut()", throwing = "exception")
  public void logException(JoinPoint joinPoint, Throwable exception) {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null) {
      HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
      StringBuilder logBuilder = (StringBuilder) request.getAttribute("logBuilder");
      // 예외가 발생한 메서드명
      String signature = String.valueOf(joinPoint.getSignature());

      // 로깅
      if (logBuilder != null) {
        logBuilder.append("\nException Occurred From : ").append(signature)
            .append(", Exception Message : ").append(exception.toString());
        log.error(logBuilder.toString());
      } else {
        log.error("\nException Occurred From : {}, Exception Message : {}",
            signature, exception.toString());
      }
    }

//    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

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
}

