package com.contrat.qualite.excelprocessing.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fichier3GroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double partDeMarche;
    private double bonus; // L'Bonus Hybrid (Inverse x Part de marché)
}