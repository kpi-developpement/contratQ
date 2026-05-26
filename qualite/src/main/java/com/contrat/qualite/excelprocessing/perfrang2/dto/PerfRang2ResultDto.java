package com.contrat.qualite.excelprocessing.perfrang2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfRang2ResultDto {
    private PerfRang2GroupDto groupA;
    private PerfRang2GroupDto groupB;
    private PerfRang2GroupDto groupC;
}