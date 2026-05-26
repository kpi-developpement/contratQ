package com.contrat.qualite.excelprocessing.constructionrang1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionGroupDto {
    private long num;
    private long denum;
    private double resultat;
}