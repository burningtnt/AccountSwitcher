package net.burningtnt.accountsx.core.config;

import net.burningtnt.accountsx.core.AccountsX;
import net.burningtnt.accountsx.core.adapters.Adapters;

import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AccountWorker {
    public interface Task {
        void run() throws Exception;
    }

    private AccountWorker() {
    }

    private static volatile long taskStartTime = -1;

    private static final Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();

    public static void submit(Task task) {
        taskQueue.add(task);
    }

    public static boolean isRunning() {
        return taskStartTime != -1 && System.currentTimeMillis() - taskStartTime >= 100;
    }

    public static Thread getWorkerThread() {
        return WORKER;
    }

    private static final Thread WORKER = new Thread((ThreadGroup) null, "AccountsX Background Thread") {
        {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Task element = taskQueue.poll();

                    if (element != null) {
                        taskStartTime = System.currentTimeMillis();

                        try {
                            element.run();
                        } catch (InterruptedException e) {
                            throw e;
                        } catch (CancellationException e) {
                            AccountsX.LOGGER.warn("Cancelled by user.", e);
                        } catch (Throwable t) {
                            AccountsX.LOGGER.warn("An exception has occurred in AccountsX Background Thread.", t);
                            Adapters.getMinecraftAdapter().showToast("as.account.action.fail.title", "as.account.action.fail.description");
                        } finally {
                            taskStartTime = -1;
                        }
                    }

                    Thread.sleep(100);
                }
            } catch (Throwable t) {
                AccountsX.LOGGER.warn("An fatal exception has occurred in the AccountsX Background Thread.", t);

                Adapters.getMinecraftAdapter().crash(new IllegalStateException("AccountsX Background Thread has occurred an exception.", t));
            }
        }
    };

    static {
        WORKER.start();
    }
}
