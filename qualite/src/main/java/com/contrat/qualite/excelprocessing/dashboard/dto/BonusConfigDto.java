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
    private ZoneConfig rang2;

    private SingleConfig sacli;
    private SingleConfig sarcli;

    private SingleConfig gemNok;
    private SingleConfig taux20j;

    // ZEDNA FICHIER 3 HNA
    private SingleConfig zmdAmii;
    private SingleConfig zmdRip;
    private SingleConfig ztd;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleConfig {
        private double min;
        private double max;
        private double bonusMax;
    }
}