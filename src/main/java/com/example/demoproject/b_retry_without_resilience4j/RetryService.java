package com.example.demoproject.b_retry_without_resilience4j;


import com.example.demoproject.exception.IgnoreException;
import com.example.demoproject.exception.RetryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RetryService {

    private static final int MAX_ATTEMPS = 3;
    private static final int WAIT_DURATION = 1000;

    /**
     * 직접 구현함으로써, Resilience4j 에서 제공하는 기능이 얼마나 가독성이 좋은지 깨닫게 됨.(Spring AOP)
     */
    public String process(String param) throws InterruptedException {
        String result = null;

        int retryCount = 0;

        while (retryCount++ < MAX_ATTEMPS) {
            try {
                result = callAnotherServer(param);
            } catch (RetryException ex) {
                if (retryCount == MAX_ATTEMPS) {
                    return fallback(param, ex);
                }

                Thread.sleep(WAIT_DURATION);
            } catch(Exception e){
                return fallback(param,e);
            }

            if (result != null)
                break;
        }

        return result;
    }

    private String fallback(String param, Exception ex) {
        // retry에 전부 실패해야 fallback이 실행
        log.info("fallback! your request is " + param);
        return "Recovered: " + ex.toString();
    }

    private String callAnotherServer(String param) {
        // retry exception은 retry된다.
//        throw new RetryException("retry exception");
        // ignore exception은 retry하지 않고 바로 예외가 클라이언트에게 전달된다.
        throw new IgnoreException("ignore exception");
    }

};
