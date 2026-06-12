package com.contrat.qualite.excelprocessing.satclisav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatcliSavResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus; // Dima present au cas où n7tajouh f l'futur
}