package com.contrat.qualite.excelprocessing.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fichier3ResponseDto {
    private ZoneGroupDto zmdAmii;
    private ZoneGroupDto zmdRip;
    private ZoneGroupDto ztd;
}