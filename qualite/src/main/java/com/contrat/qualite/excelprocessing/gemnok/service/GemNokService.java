package com.contrat.qualite.excelprocessing.gemnok.service;

import com.contrat.qualite.excelprocessing.gemnok.dto.GemNokResultDto;
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
public class GemNokService {

    public GemNokResultDto processGemNokExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            String targetCol = null;

            // Fuzzy Match bach ntfadaw l'espaces f l'entête
            for (String header : headerMap.keySet()) {
                if (header.trim().toLowerCase().contains("tx gem nok") || header.trim().toLowerCase().contains("gem nok")) {
                    targetCol = header;
                    break;
                }
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne TX GEM NOK");

            for (CSVRecord record : parser) {
                String rawVal = record.get(targetCol);
                String val = rawVal != null ? rawVal.trim() : "";

                // Binary Filter (1 awla 0)
                boolean is1 = val.equals("1") || val.equals("1.0") || val.equals("1,0");
                boolean is0 = val.equals("0") || val.equals("0.0") || val.equals("0,0");

                if (is1 || is0) {
                    denum++; // Total dyal (1+0)
                    if (is1) {
                        num++; // Total dyal (1)
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (GEM NOK)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return GemNokResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}