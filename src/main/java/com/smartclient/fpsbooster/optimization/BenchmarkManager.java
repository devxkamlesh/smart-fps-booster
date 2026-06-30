package com.smartclient.fpsbooster.optimization;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkManager {
    private final ModConfig config;
    private boolean running = false;
    private int ticksRemaining = 0;
    private List<Integer> fpsReadings = new ArrayList<>();
    private Runnable onComplete;
    
    private static final int BENCHMARK_DURATION_TICKS = 200; // 10 seconds
    
    public BenchmarkManager(ModConfig config) {
        this.config = config;
    }
    
    public void startBenchmark(Runnable onComplete) {
        if (running) return;
        
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        
        this.running = true;
        this.ticksRemaining = BENCHMARK_DURATION_TICKS;
        this.fpsReadings.clear();
        this.onComplete = onComplete;
        
        SmartFPSBoosterClient.LOGGER.info("Benchmark started");
    }
    
    public void tick() {
        if (!running) return;
        
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            cancel();
            return;
        }
        
        // Record FPS every tick
        fpsReadings.add(client.getFps());
        
        ticksRemaining--;
        
        if (ticksRemaining <= 0) {
            finishBenchmark();
        }
    }
    
    private void finishBenchmark() {
        running = false;
        
        if (fpsReadings.isEmpty()) {
            SmartFPSBoosterClient.LOGGER.warn("Benchmark finished with no readings");
            return;
        }
        
        // Calculate results
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        
        for (int fps : fpsReadings) {
            sum += fps;
            min = Math.min(min, fps);
            max = Math.max(max, fps);
        }
        
        int avg = sum / fpsReadings.size();
        
        // Calculate 1% low
        List<Integer> sorted = new ArrayList<>(fpsReadings);
        sorted.sort(Integer::compareTo);
        int onePercentIndex = Math.max(0, sorted.size() / 100);
        int onePercentLow = sorted.get(onePercentIndex);
        
        // Score = weighted average (avg * 0.6 + 1%low * 0.3 + min * 0.1)
        int score = (int) (avg * 0.6 + onePercentLow * 0.3 + min * 0.1);
        
        config.setLastBenchmarkScore(score);
        config.setLastBenchmarkTime(System.currentTimeMillis());
        
        SmartFPSBoosterClient.LOGGER.info("Benchmark complete: Score={}, Avg={}, 1%Low={}, Min={}, Max={}", 
            score, avg, onePercentLow, min, max);
        
        if (onComplete != null) {
            onComplete.run();
        }
    }
    
    public void cancel() {
        running = false;
        ticksRemaining = 0;
        fpsReadings.clear();
    }
    
    public boolean isRunning() { return running; }
    public int getTicksRemaining() { return ticksRemaining; }
    public int getTotalTicks() { return BENCHMARK_DURATION_TICKS; }
    public float getProgress() { 
        return 1.0f - ((float) ticksRemaining / BENCHMARK_DURATION_TICKS); 
    }
    
    public int getCurrentReadingCount() { return fpsReadings.size(); }
    
    public int getLiveAverage() {
        if (fpsReadings.isEmpty()) return 0;
        int sum = 0;
        for (int fps : fpsReadings) sum += fps;
        return sum / fpsReadings.size();
    }
}
