package com.contrat.qualite.excelprocessing.hotlinerang1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotlineRang1ResultDto {
    private HotlineGroupDto groupA;
    private HotlineGroupDto groupB;
    private HotlineGroupDto groupC;
}