package com.contrat.qualite.excelprocessing.sacliok.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.sacliok.dto.SacliResultDto;
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
public class SacliService {

    private static final String EXPECTED_VALR = "valr not glbl";

    public SacliResultDto processSacliExcel(MultipartFile file, String configJson) {
        long num = 0;
        long denum = 0;

        // Extraction dyal l'Config
        BonusConfigDto.SingleConfig config = parseSacliConfig(configJson);

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

                if (valr.equals("5") || valr.equals("5.0") || valr.equals("5,0")) {
                    num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SACLI OK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        // Calcul Resultat
        double resultat = 0.0;
        if (denum > 0) {
            resultat = Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
        }

        // Calcul Bonus (Pure Logic)
        double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

        return SacliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(bonus)
                .build();
    }

    // ================= HELPERS ================= //

    private BonusConfigDto.SingleConfig parseSacliConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getSacli() != null) return fullConfig.getSacli();
            } catch (Exception e) {
                log.error("Erreur parsing config SACLI, fallback to default", e);
            }
        }
        // Valeurs Initiales li 3titini l SACLI OK
        return new BonusConfigDto.SingleConfig(80.0, 95.0, 2.0);
    }

    // LA RÈGLE D'OR (PURE INTERPOLATION BLA PART DE MARCHÉ)
    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;

        // Règle de 3
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}