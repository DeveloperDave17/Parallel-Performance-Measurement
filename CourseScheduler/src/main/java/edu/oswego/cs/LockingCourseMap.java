package edu.oswego.cs;

import java.util.List;
import java.util.HashMap;
import java.util.concurrent.locks.StampedLock;

public class LockingCourseMap {
  
  private HashMap<String,Course> courseHashMap = new HashMap<>();
  
  private final StampedLock sl = new StampedLock();

  public void put(String courseId, Course course) throws InterruptedException {
    Long stamp = sl.writeLock();
    try {
      courseHashMap.put(courseId, course);
    } finally {
      sl.unlockWrite(stamp);
    }
  }

  public Course getCourse(String courseId) {
    long stamp = sl.tryOptimisticRead();
    Course course = courseHashMap.get(courseId);
    if (!sl.validate(stamp)) {
      stamp = sl.readLock();
      try {
        course = courseHashMap.get(courseId);
      } finally {
        sl.unlockRead(stamp);
      }
    }
    return course;
  }

  public List<Course> getAllCourses() {
    long stamp = sl.tryOptimisticRead();
    List<Course> courses = courseHashMap.values().stream().toList();
    if (!sl.validate(stamp)) {
      stamp = sl.readLock();
      try {
        courses = courseHashMap.values().stream().toList();
      } finally {
        sl.unlockRead(stamp);
      }
    }
    return courses;
  }

  
}
