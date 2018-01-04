package com.brandon3055.homesweethome.util;

import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.homesweethome.HomeSweetHome;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class DelayedTask {

    public static void run(int delay, Runnable task) {
        HomeSweetHome.proxy.runSidedProcess(new Task(delay, task));
    }

    public static class Task implements IProcess {
        private int delay;
        private Runnable task;
        private boolean hasExecuted = false;

        public Task(int delay, Runnable task) {
            this.delay = delay;
            this.task = task;
        }


        @Override
        public final void updateProcess() {
            if (delay <= 0 && !hasExecuted) {
                task.run();
                hasExecuted = true;
            }
            delay--;
        }

        @Override
        public boolean isDead() {
            return hasExecuted;
        }
    }

}
