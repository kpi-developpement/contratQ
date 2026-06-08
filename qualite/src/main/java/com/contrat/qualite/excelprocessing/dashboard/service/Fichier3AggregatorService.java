package com.contrat.qualite.excelprocessing.dashboard.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier3GroupDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier3ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class Fichier3AggregatorService {

    public Fichier3ResponseDto processFichier3(MultipartFile file, String configJson) {
        long amiiNum = 0, amiiDenum = 0;
        long ripNum = 0, ripDenum = 0;
        long ztdNum = 0, ztdDenum = 0;

        BonusConfigDto config = parseConfigOrDefault(configJson);

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            String actualZoneCol = null, actualStatutCol = null;

            // Fuzzy Match pour les colonnes (À adapter 3la 7ssab l'fichier dyalk b dbt)
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("zone") || cleanHeader.contains("type")) actualZoneCol = header;
                if (cleanHeader.contains("statut") || cleanHeader.contains("result")) actualStatutCol = header;
            }

            for (CSVRecord record : parser) {
                if (actualZoneCol != null && actualStatutCol != null
                        && record.isMapped(actualZoneCol) && record.isMapped(actualStatutCol)) {

                    String rawZone = record.get(actualZoneCol);
                    String rawStatut = record.get(actualStatutCol);

                    String zone = rawZone != null ? rawZone.toUpperCase().trim() : "";
                    String statut = rawStatut != null ? rawStatut.toUpperCase().trim() : "";

                    // L'condition dyal l'OK (NUM) -> À adapter 3la 7ssab logique Fichier 3 dyalk
                    boolean isOk = statut.equals("OK") || statut.equals("1");

                    if (zone.contains("AMII")) {
                        amiiDenum++;
                        if (isOk) amiiNum++;
                    } else if (zone.contains("RIP")) {
                        ripDenum++;
                        if (isOk) ripNum++;
                    } else if (zone.contains("ZTD")) {
                        ztdDenum++;
                        if (isOk) ztdNum++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal Fichier 3", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        // 1. CALCUL TOTAL DENUM (Pour Part de Marché)
        long totalDenum = amiiDenum + ripDenum + ztdDenum;

        // 2. BUILD DES RÉSULTATS AVEC L'HYBRID FORMULA
        return Fichier3ResponseDto.builder()
                .zmdAmii(buildGroup(amiiNum, amiiDenum, totalDenum, config.getZmdAmii()))
                .zmdRip(buildGroup(ripNum, ripDenum, totalDenum, config.getZmdRip()))
                .ztd(buildGroup(ztdNum, ztdDenum, totalDenum, config.getZtd()))
                .build();
    }

    // ================= HELPERS & CONFIG ================= //

    private BonusConfigDto parseConfigOrDefault(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(configJson, BonusConfigDto.class);
            } catch (Exception e) {
                log.error("Error parsing config, falling back to defaults", e);
            }
        }
        BonusConfigDto defaultConfig = new BonusConfigDto();
        // Valeurs Initiales dyal Fichier 3 (L'khayba 10%, L'mzyana 6%, BonusMax 2%)
        BonusConfigDto.SingleConfig defaultF3Config = new BonusConfigDto.SingleConfig(10.0, 6.0, 2.0);
        defaultConfig.setZmdAmii(defaultF3Config);
        defaultConfig.setZmdRip(defaultF3Config);
        defaultConfig.setZtd(defaultF3Config);
        return defaultConfig;
    }

    private double calc(long num, long denum) {
        if (denum == 0) return 0.0;
        return Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
    }

    private double calcPart(long localDenum, long globalDenum) {
        if (globalDenum == 0) return 0.0;
        return Math.round((((double) localDenum / globalDenum) * 100) * 100.0) / 100.0;
    }

    // L'FORMULE HYBRIDE : Inverse (Lower is better) * Part de Marché
    private double calcInverseBonusWithPart(double resultat, double partDeMarche, double pointMin, double pointMax, double bonusMax) {
        double partRatio = partDeMarche / 100.0;

        // Point MIN hna = 10% (L'khayba), Point MAX = 6% (L'mzyana)
        if (resultat >= pointMin) return 0.0;
        if (resultat <= pointMax) return Math.round((bonusMax * partRatio) * 100.0) / 100.0;

        // Interpolation Règle de 3
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax * partRatio;
        return Math.round(bonus * 100.0) / 100.0;
    }

    private Fichier3GroupDto buildGroup(long num, long denum, long totalDenum, BonusConfigDto.SingleConfig conf) {
        double res = calc(num, denum);
        double part = calcPart(denum, totalDenum);
        double bonus = calcInverseBonusWithPart(res, part, conf.getMin(), conf.getMax(), conf.getBonusMax());
        return new Fichier3GroupDto(num, denum, res, part, bonus);
    }
}