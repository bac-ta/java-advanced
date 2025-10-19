package volatide;

public class Deadlock {

    static class Resource {

    }

    private final Resource r1 = new Resource();
    private final Resource r2 = new Resource();

    public void method1() {
        synchronized (r1) {
            System.out.println("Thread 1 locked r1");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            synchronized (r2) {
                System.out.println("Thread 1 locked r2");
            }
        }
    }

    public void method2() {
        synchronized (r2) {
            System.out.println("Thread 2 locked r2");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            synchronized (r1) {
                System.out.println("Thread 2 locked r1");
            }
        }
    }

    public static void main(String[] args) {
        var deadlock = new Deadlock();
        new Thread(deadlock::method1).start();
        new Thread(deadlock::method2).start();
    }
}
