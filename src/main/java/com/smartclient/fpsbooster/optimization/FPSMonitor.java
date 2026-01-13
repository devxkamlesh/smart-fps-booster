package com.smartclient.fpsbooster.optimization;

import java.util.LinkedList;

public class FPSMonitor {
    private static final int HISTORY_SIZE = 120;
    private static final int GRAPH_SIZE = 60;
    private final LinkedList<Integer> fpsHistory = new LinkedList<>();
    private final LinkedList<Integer> fpsGraphData = new LinkedList<>();
    
    private int currentFps = 0;
    private int averageFps = 0;
    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private int onePercentLow = 0;
    
    // Memory
    private long usedMemoryMB = 0;
    private long maxMemoryMB = 0;
    private float memoryPercent = 0;
    
    // Frame time
    private float frameTimeMs = 0;
    private int tickCounter = 0;
    
    public void update(int fps) {
        this.currentFps = fps;
        this.frameTimeMs = fps > 0 ? 1000f / fps : 0;
        
        fpsHistory.addLast(fps);
        if (fpsHistory.size() > HISTORY_SIZE) {
            fpsHistory.removeFirst();
        }
        
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            fpsGraphData.addLast(fps);
            if (fpsGraphData.size() > GRAPH_SIZE) {
                fpsGraphData.removeFirst();
            }
        }
        
        calculateStats();
        updateMemory();
    }
    
    private void calculateStats() {
        if (fpsHistory.isEmpty()) return;
        
        int sum = 0;
        minFps = Integer.MAX_VALUE;
        maxFps = 0;
        
        for (int fps : fpsHistory) {
            sum += fps;
            if (fps < minFps) minFps = fps;
            if (fps > maxFps) maxFps = fps;
        }
        
        averageFps = sum / fpsHistory.size();
        
        // Calculate 1% low
        LinkedList<Integer> sorted = new LinkedList<>(fpsHistory);
        sorted.sort(Integer::compareTo);
        int lowCount = Math.max(1, sorted.size() / 100);
        int lowSum = 0;
        for (int i = 0; i < lowCount; i++) {
            lowSum += sorted.get(i);
        }
        onePercentLow = lowSum / lowCount;
    }
    
    private void updateMemory() {
        Runtime runtime = Runtime.getRuntime();
        maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        memoryPercent = maxMemoryMB > 0 ? (float) usedMemoryMB / maxMemoryMB * 100 : 0;
    }
    
    public boolean isStable() {
        if (fpsHistory.size() < 10) return true;
        if (averageFps == 0) return true;
        return (maxFps - minFps) < averageFps * 0.3;
    }
    
    public boolean isBelowTarget(int targetFps) {
        return averageFps < targetFps * 0.8;
    }
    
    public boolean isAboveTarget(int targetFps) {
        return averageFps > targetFps * 1.2;
    }
    
    // Getters
    public int getCurrentFps() { return currentFps; }
    public int getAverageFps() { return averageFps; }
    public int getMinFps() { return minFps == Integer.MAX_VALUE ? 0 : minFps; }
    public int getMaxFps() { return maxFps; }
    public int getOnePercentLow() { return onePercentLow; }
    public long getUsedMemoryMB() { return usedMemoryMB; }
    public long getMaxMemoryMB() { return maxMemoryMB; }
    public float getMemoryPercent() { return memoryPercent; }
    public float getFrameTimeMs() { return frameTimeMs; }
    public float getCpuUsage() { return -1; } // Not available without OSHI
    public float getGpuUsage() { return -1; }
    public LinkedList<Integer> getFpsHistory() { return fpsHistory; }
    public LinkedList<Integer> getFpsGraphData() { return fpsGraphData; }
}
