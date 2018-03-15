package com.gjf.wc.utils;

import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by guojunfu on 18/3/15.
 */

public class ThreadPool {
    private static final boolean IS_DEBUG = false;
    private static ScheduledExecutorService sScheduledPool = Executors.newScheduledThreadPool(1);
    private static ScheduledExecutorService sSingleExecutor = Executors.newSingleThreadScheduledExecutor();
    private static Handler sUiHandler = null;

    public ThreadPool() {
    }

    public static Future<?> runOnPool(Runnable r) {
        return sScheduledPool.submit(new ThreadPool.REHandler(r));
    }

    public static void runOnUi(Runnable r) {
        if(sUiHandler == null) {
            sUiHandler = new Handler(GlobalConfig.getAppContext().getMainLooper());
        }

        sUiHandler.post(r);
    }

    public static void postOnUiDelayed(Runnable r, long delay) {
        if(sUiHandler == null) {
            sUiHandler = new Handler(GlobalConfig.getAppContext().getMainLooper());
        }

        sUiHandler.postDelayed(r, delay);
    }

    public static void runOnWorker(Runnable r) {
        sSingleExecutor.submit(new ThreadPool.REHandler(r));
    }

    public static void postOnWorkerDelayed(Runnable r, int delay) {
        sSingleExecutor.schedule(r, (long)delay, TimeUnit.MILLISECONDS);
    }

    public static void postOnPoolDelayed(Runnable r, int delay) {
        sScheduledPool.schedule(r, (long)delay, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        sScheduledPool.shutdown();
        sSingleExecutor.shutdown();
    }

    private static class REHandler implements Runnable {
        Runnable delegate;

        public REHandler(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (RuntimeException var2) {
                var2.printStackTrace();
            }

        }
    }
}