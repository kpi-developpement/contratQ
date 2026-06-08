package com.contrat.qualite.excelprocessing.sacliok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SacliResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus; // <-- L'BONUS TZAD HNA
}