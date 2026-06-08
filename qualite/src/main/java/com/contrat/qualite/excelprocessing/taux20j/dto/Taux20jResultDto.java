package com.contrat.qualite.excelprocessing.taux20j.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Taux20jResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus; // <-- L'BONUS TZAD
}