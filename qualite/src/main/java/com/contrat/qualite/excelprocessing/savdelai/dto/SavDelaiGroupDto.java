package com.contrat.qualite.excelprocessing.savdelai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavDelaiGroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double bonus;
}