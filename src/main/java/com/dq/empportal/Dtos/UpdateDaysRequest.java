package com.dq.empportal.Dtos;

import java.time.LocalDate;
import java.util.List;

public class UpdateDaysRequest {
    private List<LocalDate> leaveDays;
    private List<LocalDate> holidays;
    private List<LocalDate> nonBillableDays;

    // Getters and Setters
    public List<LocalDate> getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(List<LocalDate> leaveDays) {
        this.leaveDays = leaveDays;
    }

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public List<LocalDate> getNonBillableDays() {
        return nonBillableDays;
    }

    public void setNonBillableDays(List<LocalDate> nonBillableDays) {
        this.nonBillableDays = nonBillableDays;
    }
}

