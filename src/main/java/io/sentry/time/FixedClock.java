package io.sentry.time;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FixedClock implements Clock {
    private Date date;

    public FixedClock(Date date) {
        this.date = date;
    }

    public long millis() {
        return this.date.getTime();
    }

    public Date date() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void tick(long j, TimeUnit timeUnit) {
        this.date = new Date(this.date.getTime() + timeUnit.toMillis(j));
    }
}
