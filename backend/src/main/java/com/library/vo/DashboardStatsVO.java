package com.library.vo;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long borrowedCopies;
    private long activeStudents;
    private long activeCards;
    private long currentBorrowings;
    private long unpaidFines;
    private java.math.BigDecimal unpaidFineAmount;
}
