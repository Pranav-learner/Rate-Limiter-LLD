package com.ratelimiter;

import com.ratelimiter.model.RateLimiterConfig;
import com.ratelimiter.model.Response;
import com.ratelimiter.api.RemoteAPIService;
import com.ratelimiter.real.RealRemoteAPIService;
import com.ratelimiter.proxy.RateLimitedAPIProxy;
import com.ratelimiter.strategy.RateLimitStrategy;
import com.ratelimiter.strategy.FixedWindowStrategy;
import com.ratelimiter.strategy.SlidingWindowStrategy;
import com.ratelimiter.exception.RateLimitExceededException;

public class Main {
    private static final String CLIENT_A = "client-A";
    private static final String CLIENT_B = "client-B";
    private static final String API_ENDPOINT = "https://api.example.com";
    private static final String REQUEST_PAYLOAD = "test_request_data";
    private static final int MAX_REQUESTS_DEFAULT = 3;
    private static final int MAX_REQUESTS_TWO_CLIENTS = 2;
    private static final int WINDOW_SIZE_SECONDS = 10;
    private static final int REFILL_RATE_PER_SECOND = 1;
    private static final int CALL_COUNT = 5;

    public static void main(String[] args) {
        runFixedWindowStrategyDemo();
        runSlidingWindowStrategyDemo();
        runTwoClientsDemo();
    }

    private static void runFixedWindowStrategyDemo() {
        System.out.println("=== FixedWindowStrategy ===");
        RateLimiterConfig config = new RateLimiterConfig(MAX_REQUESTS_DEFAULT, WINDOW_SIZE_SECONDS,
                REFILL_RATE_PER_SECOND);
        RateLimitStrategy strategy = new FixedWindowStrategy(config);
        executeClientCalls(strategy, CLIENT_A, CALL_COUNT);
    }

    private static void runSlidingWindowStrategyDemo() {
        System.out.println("=== SlidingWindowStrategy ===");
        RateLimiterConfig config = new RateLimiterConfig(MAX_REQUESTS_DEFAULT, WINDOW_SIZE_SECONDS, REFILL_RATE_PER_SECOND);
        RateLimitStrategy strategy = new SlidingWindowStrategy(config);
        executeClientCalls(strategy, CLIENT_A, CALL_COUNT);
    }

    private static void executeClientCalls(RateLimitStrategy strategy, String clientId, int callCount) {
        RemoteAPIService realService = new RealRemoteAPIService(API_ENDPOINT);
        RemoteAPIService proxy = new RateLimitedAPIProxy(realService, strategy);

        for (int index = 1; index <= callCount; index++) {
            try {
                Response response = proxy.call(clientId, REQUEST_PAYLOAD);
                System.out.println("Call " + index + " succeeded: " + response.getBody());
            } catch (RateLimitExceededException exception) {
                System.out.println("Call " + index + " blocked: " + exception.getMessage());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
            }
        }
    }

    private static void runTwoClientsDemo() {
        System.out.println("\n=== Two clients, FixedWindow ===");
        RateLimiterConfig config = new RateLimiterConfig(MAX_REQUESTS_TWO_CLIENTS, WINDOW_SIZE_SECONDS,
                REFILL_RATE_PER_SECOND);
        RateLimitStrategy strategy = new FixedWindowStrategy(config);
        RemoteAPIService realService = new RealRemoteAPIService(API_ENDPOINT);
        RemoteAPIService proxy = new RateLimitedAPIProxy(realService, strategy);

        String[] alternatingClients = new String[] {
                CLIENT_A, CLIENT_B, CLIENT_A, CLIENT_B, CLIENT_A, CLIENT_B
        };

        for (int index = 0; index < alternatingClients.length; index++) {
            String client = alternatingClients[index];
            int displayIndex = index + 1;
            try {
                Response response = proxy.call(client, REQUEST_PAYLOAD);
                System.out.println("Call " + displayIndex + " (" + client + ") succeeded: " + response.getBody());
            } catch (RateLimitExceededException exception) {
                System.out.println("Call " + displayIndex + " (" + client + ") blocked: " + exception.getMessage());
            }
        }
    }
}
