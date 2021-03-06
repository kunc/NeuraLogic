package cz.cvut.fel.ida.utils.generic;

import cz.cvut.fel.ida.utils.exporting.Exportable;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public class Timing implements Exportable {
    private static final Logger LOG = Logger.getLogger(Timing.class.getName());

    private Duration timeTaken;
    transient Instant now;

    String totalTimeTaken;
    double totalMinutes;

    long allocatedMemory;
    static long maxMemory = 0;

    public Timing() {
        setTimeTaken(Duration.ofMillis(0));
    }

    public void tic() {
        now = Instant.now();
    }

    public void toc() {
        Instant later = Instant.now();
        Duration elapsed = Duration.between(now, later);
        setTimeTaken(getTimeTaken().plus(elapsed));
        now = later;
    }

    public void checkMemory() {
        Utilities.logMemory();
        allocatedMemory = Utilities.allocatedMemory / Utilities.mb;
        if (allocatedMemory > maxMemory){
            maxMemory = allocatedMemory;
        }
    }

    public void finish() {
        totalMinutes = (double) timeTaken.toMillis() / 1000 / 60;
        totalTimeTaken = getTimeTaken().toString();
        checkMemory();
    }

    public Duration getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Duration timeTaken) {
        this.timeTaken = timeTaken;
    }
}
