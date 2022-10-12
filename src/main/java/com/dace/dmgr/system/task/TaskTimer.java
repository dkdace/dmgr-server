package com.dace.dmgr.system.task;

import com.dace.dmgr.DMGR;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class TaskTimer {
    protected final long period;
    protected final long duration;

    public TaskTimer(long period, long duration) {
        this.period = period;
        this.duration = duration;
        execute();
    }

    public TaskTimer(long period) {
        this(period, 0);
    }

    private void execute() {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (!TaskTimer.this.run(i++)) {
                    cancel();
                    onEnd(true);
                    return;
                }

                if (duration > 0)
                    if (i * period > duration) {
                        cancel();
                        onEnd(false);
                    }
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, period);
    }

    public void onEnd(boolean cancelled) {
    }

    public abstract boolean run(int i);
}
