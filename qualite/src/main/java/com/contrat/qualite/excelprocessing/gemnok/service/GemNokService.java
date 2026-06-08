package com.contrat.qualite.excelprocessing.gemnok.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.gemnok.dto.GemNokResultDto;
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
public class GemNokService {

    public GemNokResultDto processGemNokExcel(MultipartFile file, String configJson) {
        long num = 0;
        long denum = 0;

        BonusConfigDto.SingleConfig config = parseGemNokConfig(configJson);

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
                if (header.trim().toLowerCase().contains("tx gem nok") || header.trim().toLowerCase().contains("gem nok")) {
                    targetCol = header;
                    break;
                }
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne TX GEM NOK");

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
            log.error("Erreur f l'analyse dyal fichier CSV (GEM NOK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        // L'APPEL L'FORMULE INVERSE
        double bonus = calcInverseBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

        return GemNokResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(bonus)
                .build();
    }

    // ================= HELPERS ================= //

    private BonusConfigDto.SingleConfig parseGemNokConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getGemNok() != null) return fullConfig.getGemNok();
            } catch (Exception e) {
                log.error("Erreur parsing config GEM NOK", e);
            }
        }
        // Valeur initiale (L'khayba "Min" hya l'kbira, L'mzyana "Max" hya sghira)
        return new BonusConfigDto.SingleConfig(5.0, 2.0, 2.0);
    }

    // LA RÈGLE INVERSE (LOWER IS BETTER)
    private double calcInverseBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        // Point MIN hna howa l'valeur lkbira (Ex: 5%) w Point MAX hya sghira (Ex: 2%)
        if (resultat >= pointMin) return 0.0; // Jat kter mn 5% = Zéro Bonus
        if (resultat <= pointMax) return bonusMax; // Hbtat t7t 2% = Full Bonus

        // Interpolation Règle de 3
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}