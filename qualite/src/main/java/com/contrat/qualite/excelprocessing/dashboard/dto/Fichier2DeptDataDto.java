package com.contrat.qualite.excelprocessing.dashboard.dto;

import com.contrat.qualite.excelprocessing.tnh.dto.TnhResultDto;
import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfRang1ResultDto;
import com.contrat.qualite.excelprocessing.hotlinerang1.dto.HotlineRang1ResultDto;
import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionRang1ResultDto;
import com.contrat.qualite.excelprocessing.perfrang2.dto.PerfRang2ResultDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fichier2DeptDataDto {
    private TnhResultDto tnh;
    private PerfRang1ResultDto perfRang1;
    private HotlineRang1ResultDto hotlineRang1;
    private ConstructionRang1ResultDto constructionRang1;
    private PerfRang2ResultDto perfRang2;
}