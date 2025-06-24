package com.inhatc.medimate.medication;

public class MedicationItem {
    private String drugName;
    private String medicationPeriod; // "YYYY-MM-DD ~ YYYY-MM-DD" 형식
    private String schedules; // "08:00 - 1정\n13:00 - 1정" 형식

    public MedicationItem(String drugName, String medicationPeriod, String schedules) {
        this.drugName = drugName;
        this.medicationPeriod = medicationPeriod;
        this.schedules = schedules;
    }

    // Getter
    public String getDrugName() {
        return drugName;
    }

    public String getMedicationPeriod() {
        return medicationPeriod;
    }

    public String getSchedules() {
        return schedules;
    }
}
