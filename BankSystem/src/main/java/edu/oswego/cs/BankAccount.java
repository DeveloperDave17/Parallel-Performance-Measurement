package edu.oswego.cs;

public class BankAccount {
  private final String id;
  private double checkingsBalance;
  private double savingsBalance;

  public BankAccount(String id, double checkingsBalance, double savingsBalance) {
    this.id = id;
    this.checkingsBalance = checkingsBalance;
    this.savingsBalance = savingsBalance;
  }

  public synchronized double getCheckingsBalance() {
    return checkingsBalance;
  }

  public synchronized double getSavingsBalance() {
    return savingsBalance;
  }

  public synchronized double depositSavings(double amount) {
    return savingsBalance += amount;
  }

  public synchronized double depositCheckings(double amount) {
    return checkingsBalance += amount;
  }

  public synchronized double withdrawCheckings(double amount) {
    if (checkingsBalance < amount) {
      return 0;
    }
    checkingsBalance -= amount;
    return amount;
  }

  /**
   * Returns amount if transfer was successful otherwise returns -1
   */
  public synchronized double transferCheckingsToSavings(double amount) {
    if (checkingsBalance < amount) {
      return -1;
    }
    checkingsBalance -= amount;
    savingsBalance += amount;
    return amount;
  }

  /**
   * Returns amount if transfer was successful otherwise returns -1
   */
  public synchronized double transferSavingsToCheckings(double amount) {
    if (savingsBalance < amount) {
      return -1;
    }
    savingsBalance -= amount;
    checkingsBalance += amount;
    return amount;
  }

  public String getId() {
    return id;
  }

}
