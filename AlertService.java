package backend.service;

import java.util.Timer;
import java.util.TimerTask;

public class AlertService {
    private final Timer timer = new Timer(true);
    public void scheduleDaily(Runnable task) {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() { task.run(); }
        }, 0, 24L*60*60*1000);
    }
}
