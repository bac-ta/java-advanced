package volatide;

class PoliteResource {
    private volatile boolean thread1Wants = false;
    private volatile boolean thread2Wants = false;

    public void accessResource(String name, boolean isThread1) {
        int yieldCount = 0;

        while (true) {
            if (isThread1) {
                thread1Wants = true;

                // Give Thread-2 a moment to also set its flag
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }

                // Check if other thread also wants the resource
                while (thread2Wants) {
                    System.out.println(name + " sees Thread-2 wants it too, being polite and backing off...");
                    thread1Wants = false;  // Back off
                    yieldCount++;

                    // Short delay
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ignored) {
                    }

                    thread1Wants = true;  // Try again

                    // Give Thread-2 time to also set its flag again
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }

                // Acquired the resource
                System.out.println(name + " successfully acquired the resource after " + yieldCount + " yields!");
                thread1Wants = false;
                break;

            } else {
                thread2Wants = true;

                // Give Thread-1 a moment to also set its flag
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }

                while (thread1Wants) {
                    System.out.println(name + " sees Thread-1 wants it too, being polite and backing off...");
                    thread2Wants = false;
                    yieldCount++;

                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ignored) {
                    }

                    thread2Wants = true;  // Try again

                    // Give Thread-1 time to also set its flag again
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }

                System.out.println(name + " successfully acquired the resource after " + yieldCount + " yields!");
                thread2Wants = false;
                break;
            }
        }
    }
}

public class Livelock {
    public static void main(String[] args) {
        PoliteResource resource = new PoliteResource();

        Thread t1 = new Thread(() -> resource.accessResource("Thread-1", true));
        Thread t2 = new Thread(() -> resource.accessResource("Thread-2", false));

        t1.start();
        t2.start();
    }
}