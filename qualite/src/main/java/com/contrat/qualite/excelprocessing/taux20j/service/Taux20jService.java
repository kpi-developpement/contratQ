package com.contrat.qualite.excelprocessing.taux20j.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.taux20j.dto.Taux20jResultDto;
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
public class Taux20jService {

    public Taux20jResultDto processTaux20jExcel(MultipartFile file, String configJson) {
        long num = 0;
        long denum = 0;

        BonusConfigDto.SingleConfig config = parseTaux20jConfig(configJson);

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            String targetCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("inf 20") && cleanHeader.contains("rdv")) {
                    targetCol = header;
                    break;
                }
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne Volume de cmd SS-RDV inf 20");

            for (CSVRecord record : parser) {
                String rawVal = record.get(targetCol);
                String val = rawVal != null ? rawVal.trim() : "";

                boolean is1 = val.equals("1") || val.equals("1.0") || val.equals("1,0");
                boolean is0 = val.equals("0") || val.equals("0.0") || val.equals("0,0");

                if (is1 || is0) {
                    denum++;
                    if (is1) num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (Taux 20J)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        // L'APPEL L'FORMULE NORMALE
        double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

        return Taux20jResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(bonus)
                .build();
    }

    // ================= HELPERS ================= //

    private BonusConfigDto.SingleConfig parseTaux20jConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getTaux20j() != null) return fullConfig.getTaux20j();
            } catch (Exception e) {
                log.error("Erreur parsing config TAUX 20J", e);
            }
        }
        return new BonusConfigDto.SingleConfig(80.0, 95.0, 2.0); // Default values
    }

    // LA RÈGLE NORMALE (HIGHER IS BETTER)
    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;

        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}