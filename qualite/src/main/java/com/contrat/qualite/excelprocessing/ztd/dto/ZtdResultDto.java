package com.contrat.qualite.excelprocessing.ztd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZtdResultDto {
    private long num;
    private long denum;
    private double resultat;
    private double partDeMarche;
    private double bonus;
}