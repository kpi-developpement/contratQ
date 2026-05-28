package com.contrat.qualite.excelprocessing.sacliok.service;

import com.contrat.qualite.excelprocessing.sacliok.dto.SacliResultDto;
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
public class SacliService {

    // L'colonne lwe7ida li b9at katmna f SACLI
    private static final String EXPECTED_VALR = "valr not glbl";

    public SacliResultDto processSacliExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            // Fuzzy Match pour l'Header "Valr Not Glbl"
            Map<String, Integer> headerMap = parser.getHeaderMap();
            String actualValrCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains(EXPECTED_VALR)) {
                    actualValrCol = header;
                    break;
                }
            }

            if (actualValrCol == null) {
                throw new RuntimeException("Mala9inach l'colonne. Headers li tl9aw: " + headerMap.keySet());
            }

            // Parcours du fichier
            for (CSVRecord record : parser) {
                denum++; // DENUM = Total des lignes (sans header)

                String valrStr = record.get(actualValrCol);
                String valr = valrStr != null ? valrStr.trim() : "";

                // NUM = Valr Not Glbl == 5 (N9dro nl9awha 5 wla 5.0 wla 5,0)
                if (valr.equals("5") || valr.equals("5.0") || valr.equals("5,0")) {
                    num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SACLI OK)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        // Calcul du Résultat
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }

        return SacliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}