package com.contrat.qualite.excelprocessing.dashboard.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class Fichier2ResponseDto {
    private Map<String, Fichier2DeptDataDto> details;
}