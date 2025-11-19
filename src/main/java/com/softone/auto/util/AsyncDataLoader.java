package com.softone.auto.util;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 비동기 데이터 로더
 * UI 블로킹을 방지하기 위한 유틸리티 클래스
 */
public class AsyncDataLoader {
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );
    
    /**
     * 데이터를 비동기로 로드하고 UI를 업데이트
     * 
     * @param dataSupplier 데이터를 제공하는 Supplier (백그라운드 스레드에서 실행)
     * @param uiUpdater UI를 업데이트하는 Consumer (EDT에서 실행)
     * @param progressCallback 진행률 업데이트 콜백 (선택적)
     */
    public static <T> void loadAsync(
            Supplier<T> dataSupplier,
            Consumer<T> uiUpdater,
            Consumer<Integer> progressCallback) {
        
        SwingWorker<T, Integer> worker = new SwingWorker<T, Integer>() {
            @Override
            protected T doInBackground() throws Exception {
                if (progressCallback != null) {
                    publish(0);
                }
                T result = dataSupplier.get();
                if (progressCallback != null) {
                    publish(100);
                }
                return result;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                if (progressCallback != null && !chunks.isEmpty()) {
                    progressCallback.accept(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                try {
                    T result = get();
                    uiUpdater.accept(result);
                } catch (Exception e) {
                    System.err.println("비동기 데이터 로드 실패: " + e.getMessage());
                    e.printStackTrace();
                    // 오류 발생 시 빈 결과로 UI 업데이트
                    uiUpdater.accept(null);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 간단한 비동기 로드 (진행률 없음)
     */
    public static <T> void loadAsync(Supplier<T> dataSupplier, Consumer<T> uiUpdater) {
        loadAsync(dataSupplier, uiUpdater, null);
    }
    
    /**
     * CompletableFuture를 사용한 비동기 처리
     */
    public static <T> CompletableFuture<T> loadAsyncFuture(Supplier<T> dataSupplier) {
        return CompletableFuture.supplyAsync(dataSupplier, executorService);
    }
    
    /**
     * ExecutorService 종료 (애플리케이션 종료 시)
     */
    public static void shutdown() {
        executorService.shutdown();
    }
}

