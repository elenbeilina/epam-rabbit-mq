package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.exception.NotRetryableException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;

@Log4j2
@RequiredArgsConstructor
public class RetryCommand<T> {

  private static final String RETRYABLE_ERROR = "FAILED - Command failed, will be retried %s times.";
  private static final String RETRY_ERROR = "FAILED - Command failed on retry %s of %s";
  private static final String NON_RETRYABLE_ERROR = "FAILED - Command failed on all of %s retries with error: %s.";

  private final int maxRetries;
  private final long interval;

  private Long lastPollTime = 0L;

  // Takes a function and executes it, if fails, passes the function to the retry command
  public T run(Supplier<T> function) {
    try {
      return function.get();
    } catch (NotRetryableException exception) {
      throw new NotRetryableException(exception.getMessage(), exception);
    } catch (Exception e) {
      log.error(
          String.format(RETRYABLE_ERROR, maxRetries), e
      );
      return retry(function, e);
    }
  }

  @SneakyThrows
  private T retry(Supplier<T> function, Exception exception) {
    int retries = 1;

    while (retries <= maxRetries) {
      try {
        sleepIfNeeded(retries);

        return function.get();
      } catch (Exception ex) {
        log.error(String.format(RETRY_ERROR, retries, maxRetries));
        exception = ex;
        retries++;
      } finally {
        lastPollTime = currentTimeMillis();
      }
    }
    throw new NotRetryableException(
        String.format(
            NON_RETRYABLE_ERROR,
            maxRetries, exception.getMessage()
        ), exception
    );
  }

  @SneakyThrows
  private void sleepIfNeeded(int retries) {
    long millis = interval * retries - (currentTimeMillis() - lastPollTime);

    if (millis > 0) {
      Thread.sleep(millis);
    }
  }
}