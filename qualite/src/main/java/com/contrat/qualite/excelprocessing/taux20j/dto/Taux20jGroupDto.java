package com.contrat.qualite.excelprocessing.taux20j.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Taux20jGroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus;
}