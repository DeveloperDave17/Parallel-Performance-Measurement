package edu.oswego.cs;

import java.util.List;
import java.util.HashMap;
import java.util.concurrent.locks.StampedLock;

public class LockingBankMap {
  
  private HashMap<String,BankAccount> accountHashMap = new HashMap<>();
  
  private final StampedLock sl = new StampedLock();

  public void put(String id, BankAccount account) {
    Long stamp = sl.writeLock();
    try {
      accountHashMap.put(id, account);
    } finally {
      sl.unlockWrite(stamp);
    }
  }

  public BankAccount getBankAccount(String id) {
    long stamp = sl.tryOptimisticRead();
    BankAccount course = accountHashMap.get(id);
    if (!sl.validate(stamp)) {
      stamp = sl.readLock();
      try {
        course = accountHashMap.get(id);
      } finally {
        sl.unlockRead(stamp);
      }
    }
    return course;
  }
}
