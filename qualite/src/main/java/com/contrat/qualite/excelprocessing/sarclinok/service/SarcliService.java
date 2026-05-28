package com.contrat.qualite.excelprocessing.sarclinok.service;

import com.contrat.qualite.excelprocessing.sarclinok.dto.SarcliResultDto;
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
public class SarcliService {

    // L'colonne lwe7ida li b9at katmna f SARCLI
    private static final String EXPECTED_VALR = "valr not glbl";

    public SarcliResultDto processSarcliExcel(MultipartFile file) {
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

                // NUM = Valr Not Glbl == 4 awla 5
                boolean isValr4 = valr.equals("4") || valr.equals("4.0") || valr.equals("4,0");
                boolean isValr5 = valr.equals("5") || valr.equals("5.0") || valr.equals("5,0");

                if (isValr4 || isValr5) {
                    num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SARCLI NOK)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        // Calcul du Résultat
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }

        return SarcliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}