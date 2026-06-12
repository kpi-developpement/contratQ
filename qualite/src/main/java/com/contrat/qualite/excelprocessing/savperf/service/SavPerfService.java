package com.contrat.qualite.excelprocessing.savperf.service;

import com.contrat.qualite.excelprocessing.savperf.dto.SavPerfResultDto;
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
public class SavPerfService {

    public SavPerfResultDto processSavPerfExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

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
            String statutCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("statut intervention")) {
                    statutCol = header;
                    break;
                }
            }

            if (statutCol == null) throw new RuntimeException("Mala9inach l'colonne 'Statut Intervention'.");

            for (CSVRecord record : parser) {
                if (record.isMapped(statutCol)) {
                    String statutStr = record.get(statutCol);
                    String statut = statutStr != null ? statutStr.toUpperCase().trim() : "";

                    // L'Logic dyal l'Calcul
                    if ("TERMINEE_OK".equals(statut)) {
                        num++;
                        denum++;
                    } else if ("TERMINEE_KO".equals(statut)) {
                        denum++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SAV PERF)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return SavPerfResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(0.0)
                .build();
    }
}