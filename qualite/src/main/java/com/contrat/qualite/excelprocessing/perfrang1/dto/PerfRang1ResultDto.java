package com.contrat.qualite.excelprocessing.perfrang1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfRang1ResultDto {
    private PerfGroupDto groupA;
    private PerfGroupDto groupB;
    private PerfGroupDto groupC;
}