package com.contrat.qualite.excelprocessing.sarclinok.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.sarclinok.dto.SarcliResultDto;
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
public class SarcliService {

    private static final String EXPECTED_VALR = "valr not glbl";

    public SarcliResultDto processSarcliExcel(MultipartFile file, String configJson) {
        long num = 0;
        long denum = 0;

        // Extraction dyal l'Config
        BonusConfigDto.SingleConfig config = parseSarcliConfig(configJson);

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            String actualValrCol = null;

            for (String header : headerMap.keySet()) {
                if (header.trim().toLowerCase().contains(EXPECTED_VALR)) {
                    actualValrCol = header;
                    break;
                }
            }

            if (actualValrCol == null) throw new RuntimeException("Mala9inach l'colonne.");

            for (CSVRecord record : parser) {
                denum++;
                String valrStr = record.get(actualValrCol);
                String valr = valrStr != null ? valrStr.trim() : "";

                boolean isValr4 = valr.equals("4") || valr.equals("4.0") || valr.equals("4,0");
                boolean isValr5 = valr.equals("5") || valr.equals("5.0") || valr.equals("5,0");

                if (isValr4 || isValr5) {
                    num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SARCLI NOK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        // Calcul Resultat
        double resultat = 0.0;
        if (denum > 0) {
            resultat = Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
        }

        // Calcul Bonus (Pure Logic)
        double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

        return SarcliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(bonus)
                .build();
    }

    // ================= HELPERS ================= //

    private BonusConfigDto.SingleConfig parseSarcliConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getSarcli() != null) return fullConfig.getSarcli();
            } catch (Exception e) {
                log.error("Erreur parsing config SARCLI, fallback to default", e);
            }
        }
        // Valeurs Initiales li 3titini l SARCLI NOK
        return new BonusConfigDto.SingleConfig(30.0, 55.0, 1.0);
    }

    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;

        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}