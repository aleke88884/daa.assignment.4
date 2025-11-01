package org.example;
public class Metrics {
    private long startTime;
    private long endTime;
    private int dfsVisits;
    private int edgesExplored;
    private int relaxations;
    private int pushes;
    private int pops;

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        endTime = System.nanoTime();
    }

    public long getElapsedTimeNanos() {
        return endTime - startTime;
    }

    public double getElapsedTimeMillis() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public void incrementDfsVisits() {
        dfsVisits++;
    }

    public void incrementEdgesExplored() {
        edgesExplored++;
    }

    public void incrementRelaxations() {
        relaxations++;
    }

    public void incrementPushes() {
        pushes++;
    }

    public void incrementPops() {
        pops++;
    }

    public int getDfsVisits() {
        return dfsVisits;
    }

    public int getEdgesExplored() {
        return edgesExplored;
    }

    public int getRelaxations() {
        return relaxations;
    }

    public int getPushes() {
        return pushes;
    }

    public int getPops() {
        return pops;
    }

    public void reset() {

        startTime = 0;
        endTime = 0;
        dfsVisits = 0;
        edgesExplored = 0;
        relaxations = 0;
        pushes = 0;
        pops = 0;

    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{time=%.3fms, dfsVisits=%d, edges=%d, relaxations=%d, pushes=%d, pops=%d}",
                getElapsedTimeMillis(), dfsVisits, edgesExplored, relaxations, pushes, pops
        );
    }

}