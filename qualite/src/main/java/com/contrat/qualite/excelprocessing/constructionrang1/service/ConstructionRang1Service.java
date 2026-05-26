package com.contrat.qualite.excelprocessing.constructionrang1.service;

import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionGroupDto;
import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionRang1ResultDto;
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
public class ConstructionRang1Service {

    private static final String EXPECTED_ZONE = "zone_statut prise";
    private static final String EXPECTED_RANG = "rang_rdv (copie)";
    private static final String EXPECTED_STATUT = "grp_statut_crinstall_mnt";

    public ConstructionRang1ResultDto processConstructionRang1Excel(MultipartFile file) {
        long numA = 0, denumA = 0;
        long numB = 0, denumB = 0;
        long numC = 0, denumC = 0;

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            // 1. Fuzzy Match Headers
            Map<String, Integer> headerMap = parser.getHeaderMap();
            String actualZoneCol = null;
            String actualRangCol = null;
            String actualStatutCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();

                if (cleanHeader.contains(EXPECTED_ZONE)) {
                    actualZoneCol = header;
                } else if (cleanHeader.contains("rang_rdv") && cleanHeader.contains("(copie)")) {
                    actualRangCol = header;
                } else if (cleanHeader.contains(EXPECTED_STATUT)) {
                    actualStatutCol = header;
                }
            }

            if (actualZoneCol == null || actualRangCol == null || actualStatutCol == null) {
                throw new RuntimeException("Mala9inach wa7d mn les colonnes. Headers li tl9aw: " + headerMap.keySet());
            }

            for (CSVRecord record : parser) {
                String rawRang = record.get(actualRangCol);
                String rawZone = record.get(actualZoneCol);
                String rawStatut = record.get(actualStatutCol);

                // L'FIX: Kanhaydo "\n" bach "Construction\nZONE A" tweli "CONSTRUCTION ZONE A"
                String rang = rawRang != null ? rawRang.trim() : "";
                String zone = rawZone != null ?
                        rawZone.toUpperCase().replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim()
                        : "";
                String statut = rawStatut != null ? rawStatut.toUpperCase().trim() : "";

                // Conditions (CONSTRUCTION)
                boolean isRang1 = rang.equals("1") || rang.equals("1.0") || rang.equals("1,0");
                boolean isZoneA = zone.equals("CONSTRUCTION ZONE A");
                boolean isZoneB = zone.equals("CONSTRUCTION ZONE B");
                boolean isZoneC = zone.equals("CONSTRUCTION ZONE C");
                boolean isCrOk = statut.equals("CR_MNT_OK");

                if (isRang1) {
                    if (isZoneA) {
                        denumA++;
                        if (isCrOk) numA++;
                    }
                    if (isZoneB) {
                        denumB++;
                        if (isCrOk) numB++;
                    }
                    if (isZoneC) {
                        denumC++;
                        if (isCrOk) numC++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (CONSTRUCTION RANG 1)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        return ConstructionRang1ResultDto.builder()
                .groupA(buildGroupResult(numA, denumA))
                .groupB(buildGroupResult(numB, denumB))
                .groupC(buildGroupResult(numC, denumC))
                .build();
    }

    private ConstructionGroupDto buildGroupResult(long num, long denum) {
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }
        return ConstructionGroupDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}