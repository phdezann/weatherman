package org.phdezann.weather.core;

public class Sleeper {

    private Sleeper() {
    }

    public static void wait(int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
