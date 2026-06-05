package com.contrat.qualite.excelprocessing.hotlinerang1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotlineGroupDto {
    private long num;
    private long denum;
    private double resultat;
    private double partDeMarche; // <-- ZEDNA HADI
}