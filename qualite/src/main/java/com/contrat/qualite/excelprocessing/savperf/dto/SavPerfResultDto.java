package com.contrat.qualite.excelprocessing.savperf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavPerfResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus; // Dima kanwjdouh au cas où
}