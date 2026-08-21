package com.contrat.qualite.excelprocessing.gemnok.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.gemnok.dto.GemNokGroupDto;
import com.contrat.qualite.excelprocessing.gemnok.dto.GemNokResultDto;
import com.contrat.qualite.repository.KpiArchiveRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GemNokService {

    private final KpiArchiveRepository kpiArchiveRepository;

    // L'FIX: Class sghira bach ntfadaw l'mochkil dyal l'array li kay9leb num m3a denum
    private static class GemStats {
        long num = 0;
        long denum = 0;
    }

    public GemNokResultDto processGemNokExcel(MultipartFile file, String configJson, int month, int year) {
        Map<String, GemStats> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new GemStats());

        BonusConfigDto.SingleConfig config = parseGemNokConfig(configJson);

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
            String targetCol = null;
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("tx gem nok") || cleanHeader.contains("gem nok")) {
                    targetCol = header;
                }
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) {
                    actualDeptCol = header;
                }
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne TX GEM NOK");

            for (CSVRecord record : parser) {
                if (record.isMapped(targetCol)) {
                    String rawVal = record.get(targetCol);
                    String val = rawVal != null ? rawVal.trim().toLowerCase() : "";

                    String dept = "INCONNU";
                    if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                        String rawDept = record.get(actualDeptCol);
                        dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                    }

                    // L'FIX: Filtre s7i7 100% l ga3 les formats dyal Excel
                    boolean is1 = val.equals("1") || val.startsWith("1.") || val.startsWith("1,") || val.equals("vrai") || val.equals("true");
                    boolean is0 = val.equals("0") || val.startsWith("0.") || val.startsWith("0,") || val.equals("faux") || val.equals("false");

                    if (is1 || is0) {
                        statsMap.putIfAbsent(dept, new GemStats());

                        // DENUM: Kayakhod total dyal les lignes li fihom 1 wla 0
                        statsMap.get("GLOBAL").denum++;
                        statsMap.get(dept).denum++;

                        // NUM: Kayakhod GHIIR les lignes li fihom 1
                        if (is1) {
                            statsMap.get("GLOBAL").num++;
                            statsMap.get(dept).num++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (GEM NOK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "GEM_NOK");

        Map<String, GemNokGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, GemStats> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue().num;
            long denum = entry.getValue().denum;

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;
            double bonus = calcInverseBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

            finalDetails.put(dept, new GemNokGroupDto(num, denum, resultat, bonus));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("GEM_NOK").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(bonus).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return GemNokResultDto.builder().details(finalDetails).build();
    }

    private BonusConfigDto.SingleConfig parseGemNokConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getGemNok() != null) return fullConfig.getGemNok();
            } catch (Exception e) { log.error("Erreur parsing config GEM NOK", e); }
        }
        return new BonusConfigDto.SingleConfig(5.0, 2.0, 2.0);
    }

    private double calcInverseBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat >= pointMin) return 0.0;
        if (resultat <= pointMax) return bonusMax;
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}