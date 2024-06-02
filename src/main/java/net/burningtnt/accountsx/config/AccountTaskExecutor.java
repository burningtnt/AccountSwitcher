package net.burningtnt.accountsx.config;

import net.burningtnt.accountsx.AccountsX;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AccountTaskExecutor {
    public interface TaskRunnable {
        void run() throws Exception;
    }

    public enum State {
        QUEUED, RUNNING, DONE, FAILED
    }

    public static final class Task {
        private volatile State state = State.QUEUED;

        private final TaskRunnable task;

        private Task(TaskRunnable task) {
            this.task = task;
        }

        public State getState() {
            return state;
        }
    }

    private AccountTaskExecutor() {
    }

    private static volatile long startTime = -1;

    public static boolean isRunning() {
        return startTime != -1 && System.currentTimeMillis() - startTime >= 100;
    }

    private static final Queue<Task> queueList = new ConcurrentLinkedQueue<>();

    public static void submit(TaskRunnable task) {
        queueList.add(new Task(task));
    }

    static {
        new Thread((ThreadGroup) null, "AccountSwitcher OperationThread") {
            {
                setDaemon(true);
            }

            @Override
            public void run() {
                while (true) {
                    Task element = queueList.poll();

                    if (element != null) {
                        element.state = AccountTaskExecutor.State.RUNNING;
                        startTime = System.currentTimeMillis();

                        try {
                            element.task.run();
                            element.state = AccountTaskExecutor.State.DONE;
                        } catch (InterruptedException e) {
                            return;
                        } catch (Throwable t) {
                            AccountsX.LOGGER.warn("An exception occured in background thread.", t);
                            element.state = AccountTaskExecutor.State.FAILED;
                        } finally {
                            startTime = -1;
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();
    }
}
