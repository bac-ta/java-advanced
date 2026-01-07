package concurency;

import java.util.concurrent.locks.ReentrantLock;

public class SharedResourceByReentrantLock {

    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;

    public void increment() {
        lock.lock(); // Acquire the lock
        try {
            counter++; // Critical section
        } finally {
            lock.unlock(); // Always release the lock in a finally block
        }
    }

    public int getCounter() {
        return counter;

    }
}

