package volatide;

public class Volatide {
    volatile boolean running = true;

    void start() {
        new Thread(() -> {
            while (running) {
                System.out.println(Thread.currentThread().getName());
            }
        }).start();
    }

    void stop() {
        running = false;
    }
}
