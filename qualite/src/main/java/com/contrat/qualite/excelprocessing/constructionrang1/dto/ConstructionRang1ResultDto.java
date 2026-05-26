package com.contrat.qualite.excelprocessing.constructionrang1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionRang1ResultDto {
    private ConstructionGroupDto groupA;
    private ConstructionGroupDto groupB;
    private ConstructionGroupDto groupC;
}