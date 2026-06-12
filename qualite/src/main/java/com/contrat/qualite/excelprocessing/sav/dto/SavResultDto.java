package com.contrat.qualite.excelprocessing.sav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus; // Khlinaha blast'ha wajda au cas où bghiti tzid l'bonus l SAV mn b3d
}