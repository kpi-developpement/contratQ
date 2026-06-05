package com.contrat.qualite.excelprocessing.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusConfigDto {
    private ZoneConfig plp;
    private ZoneConfig hotline;
    private ZoneConfig construction;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneConfig {
        private MinMax a;
        private MinMax b;
        private MinMax c;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinMax {
        private double min;
        private double max;
    }
}