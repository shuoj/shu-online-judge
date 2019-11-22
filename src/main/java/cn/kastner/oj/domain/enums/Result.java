package cn.kastner.oj.domain.enums;

public enum Result {
  ACCEPTED,
  RUNTIME_ERROR,
  CPU_TIME_LIMIT_EXCEEDED,
  TIME_LIMIT_EXCEEDED,
  MEMORY_LIMIT_EXCEEDED,
  COMPILE_ERROR,
  WRONG_ANSWER,
  SYSTEM_ERROR,
  WAITING,
  JUDGE_CLIENT_ERROR;

  public static boolean isUserError(Result result) {
    return RUNTIME_ERROR.equals(result) ||
        CPU_TIME_LIMIT_EXCEEDED.equals(result) ||
        TIME_LIMIT_EXCEEDED.equals(result) ||
        MEMORY_LIMIT_EXCEEDED.equals(result) ||
        WRONG_ANSWER.equals(result);
  }
}
