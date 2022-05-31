package com.poc.httpConnectionPooling.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static com.poc.httpConnectionPooling.configuration.DemoConfiguration.CONCURRENCY;
@Service
public class DemoService {

    private static final int SAMPLE_SIZE = 10000;

    RestTemplate restTemplate;

    public DemoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void execute() {

        System.out.println("Starting...");

        AtomicInteger counter = new AtomicInteger(-1);
        long[] time = new long[SAMPLE_SIZE];

        Runnable runnableTask = () -> {
            try {
                ThreadLocal<Long> startTime = new ThreadLocal<>();
                startTime.set(System.currentTimeMillis());

                String responseString = this.restTemplate.getForObject("https://reqres.in/api/users?page=4", String.class);

                time[counter.incrementAndGet()] = (System.currentTimeMillis() - startTime.get());

                Thread currentThread = Thread.currentThread();
                System.out.println("Thread – id:" + currentThread.getId() + ", name:" + currentThread.getName() + ", elapsedTime:" + time[counter.get()] + ", response:" + responseString);
            } catch (Exception e) {
                System.out.println("error: " + e.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENCY);
        LongStream.range(0, SAMPLE_SIZE).forEach(integer -> {
            try {
                executorService.execute(runnableTask); // submits task to executors queue
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        executorService.shutdown();

        while (!executorService.isTerminated()) {
            Thread.onSpinWait();
        }

        print(time);
        System.out.println("Completed...");

    }

    private void print(long[] time) {
        Arrays.stream(time).forEach(System.out::println);
    }

}