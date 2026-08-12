package com.contrat.qualite.excelprocessing.ccr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcrGroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus;
}