package com.contrat.qualite.excelprocessing.dashboard.dto;

import com.contrat.qualite.excelprocessing.sacli.dto.SacliResultDto;
import com.contrat.qualite.excelprocessing.sarcli.dto.SarcliResultDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fichier1ResponseDto {
    private SacliResultDto sacli;
    private SarcliResultDto sarcli;
}