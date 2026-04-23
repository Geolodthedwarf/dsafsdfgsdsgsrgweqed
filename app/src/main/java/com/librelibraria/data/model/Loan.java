package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "loans",
        foreignKeys = {
                @ForeignKey(
                        entity = Book.class,
                        parentColumns = "id",
                        childColumns = "bookId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Borrower.class,
                        parentColumns = "id",
                        childColumns = "borrowerId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("bookId"),
                @Index("borrowerId")
        }
)
public class Loan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long bookId;
    private Long borrowerId;
    private String borrowerName;
    private long loanDate;
    private long dueDate;
    private long returnDate;
    private LoanStatus status;
    private String conditionOnLoan;
    private String conditionOnReturn;
    private double lateFee;
    private String notes;
    private int renewalCount;
    private long lastModified;
    private boolean isSynced;

    public Loan() {
        this.loanDate = System.currentTimeMillis();
        this.status = LoanStatus.ACTIVE;
        this.renewalCount = 0;
        this.lastModified = System.currentTimeMillis();
        this.isSynced = false;
        this.dueDate = loanDate + (14L * 24 * 60 * 60 * 1000);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public long getLoanDate() { return loanDate; }
    public void setLoanDate(long loanDate) { this.loanDate = loanDate; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public long getReturnDate() { return returnDate; }
    public void setReturnDate(long returnDate) { this.returnDate = returnDate; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public String getConditionOnLoan() { return conditionOnLoan; }
    public void setConditionOnLoan(String conditionOnLoan) { this.conditionOnLoan = conditionOnLoan; }

    public String getConditionOnReturn() { return conditionOnReturn; }
    public void setConditionOnReturn(String conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getRenewalCount() { return renewalCount; }
    public void setRenewalCount(int renewalCount) { this.renewalCount = renewalCount; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isOverdue() {
        if (status != LoanStatus.ACTIVE) return false;
        return System.currentTimeMillis() > dueDate;
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return (System.currentTimeMillis() - dueDate) / (24 * 60 * 60 * 1000);
    }

    public double calculateLateFee() {
        if (!isOverdue()) return 0;
        return getDaysOverdue() * 0.50;
    }
}