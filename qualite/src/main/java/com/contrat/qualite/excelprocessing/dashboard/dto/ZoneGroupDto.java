package com.contrat.qualite.excelprocessing.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZoneGroupDto {
    private long num;
    private long denum;
    private double resultat;
}