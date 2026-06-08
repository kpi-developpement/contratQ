package com.contrat.qualite.excelprocessing.perfrang2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfRang2GroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double partDeMarche;
    private double bonus; // <-- L'BONUS TZAD HNA
}