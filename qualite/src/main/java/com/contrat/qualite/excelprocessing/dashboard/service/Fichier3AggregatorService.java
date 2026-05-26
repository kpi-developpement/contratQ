package com.contrat.qualite.excelprocessing.dashboard.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier3ResponseDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.ZoneGroupDto;
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

    public Fichier3ResponseDto processFichier3(MultipartFile file) {
        // ZMD AMII
        long zmdAmiiNum = 0, zmdAmiiDenum = 0;
        // ZMD RIP
        long ripNum = 0, ripDenum = 0;
        // ZTD
        long ztdNum = 0, ztdDenum = 0;

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
            String actualZoneCol = null;
            String actualTauxCol = null;

            // 1. Recherche Dynamique dyal les colonnes (Fuzzy Match Anti-Crash)
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                // Kanchofou wach "Zone Ftth Comex"
                if (cleanHeader.contains("zone") && cleanHeader.contains("ftth")) actualZoneCol = header;
                    // Kanchofou wach "Taux de report TH"
                else if (cleanHeader.contains("taux") && cleanHeader.contains("report") && cleanHeader.contains("th")) actualTauxCol = header;
            }

            if (actualZoneCol == null || actualTauxCol == null) {
                throw new RuntimeException("Mala9inach les colonnes mtloubin (Zone Ftth Comex awla Taux de report TH)");
            }

            // 2. L'Boucle O(N) dyal l'Calcul
            for (CSVRecord record : parser) {
                if (record.isMapped(actualZoneCol) && record.isMapped(actualTauxCol)) {

                    String rawZone = record.get(actualZoneCol);
                    String rawTaux = record.get(actualTauxCol);

                    String zone = rawZone != null ? rawZone.toUpperCase().replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim() : "";
                    String taux = rawTaux != null ? rawTaux.trim() : "";

                    // L'calcul kaydar GHA ILA kan l'taux fih 0 awla 1
                    if (taux.equals("0") || taux.equals("0.0") || taux.equals("1") || taux.equals("1.0")) {
                        boolean isUn = taux.equals("1") || taux.equals("1.0");

                        // Application des règles 3la 7sab l'Zone
                        if (zone.contains("ZMD RIP")) {
                            ripDenum++;
                            if (isUn) ripNum++;
                        }
                        else if (zone.contains("ZMD AMII")) {
                            zmdAmiiDenum++;
                            if (isUn) zmdAmiiNum++;
                        }
                        else if (zone.contains("ZTD")) {
                            ztdDenum++;
                            if (isUn) ztdNum++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal Fichier 3 (ZMD/ZTD/RIP)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        return Fichier3ResponseDto.builder()
                .zmdAmii(buildZoneGroup(zmdAmiiNum, zmdAmiiDenum))
                .zmdRip(buildZoneGroup(ripNum, ripDenum))
                .ztd(buildZoneGroup(ztdNum, ztdDenum))
                .build();
    }

    private ZoneGroupDto buildZoneGroup(long num, long denum) {
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }
        return ZoneGroupDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}