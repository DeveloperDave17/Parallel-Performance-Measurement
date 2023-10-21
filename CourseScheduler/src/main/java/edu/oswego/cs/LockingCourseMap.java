package edu.oswego.cs;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockingCourseMap {
  
  private HashMap<String,Course> courseHashMap = new HashMap<>();
  
  private final ReentrantLock lock = new ReentrantLock();

  private Condition writeable = lock.newCondition();

  private int readers, writers, waitingReaders, waitingWriters;

  public void put(String courseId, Course course) throws InterruptedException {
    lock.lock();
    try {
      ++waitingWriters;
      while (readers != 0 || writers != 0) {
        writeable.await();
      }
      ++writers;

    } finally {
      lock.unlock();
    }
  }
}
