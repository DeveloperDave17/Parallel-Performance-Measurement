package edu.oswego.cs;

import java.util.List;

public class Course {
  private String identifier;
  private String name;
  private String description;
  private int count;
  private int maxCapacity;
  private String startTime;
  private String endTime;
  private List<String> studentIds;

  public Course(String identifier, String name, String description, int maxCapacity, String startTime, String endTime) {
    this.identifier = identifier;
    this.name = name;
    this.description = description;
    this.count = 0;
    this.maxCapacity = maxCapacity;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getCount() {
    return count;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndtime() {
    return endTime;
  }

  public synchronized boolean registerStudent(String studentId) {
    if (count < maxCapacity) {
      count++;
      studentIds.add(studentId);
      return true;
    }
    return false;
  }

  public synchronized boolean deregisterStudent(String studentId) {
    if (studentIds.remove(studentId)) {
      count--;
      return true;
    }
    return false;
  }
}
