package com.contrat.qualite.excelprocessing.sarclinok.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.sarclinok.dto.SarcliGroupDto;
import com.contrat.qualite.excelprocessing.sarclinok.dto.SarcliResultDto;
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
public class SarcliService {

    private static final String EXPECTED_VALR = "valr not glbl";
    private final KpiArchiveRepository kpiArchiveRepository;

    public SarcliResultDto processSarcliExcel(MultipartFile file, String configJson, int month, int year) {
        Map<String, long[]> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new long[]{0, 0});

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
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains(EXPECTED_VALR)) actualValrCol = header;
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            if (actualValrCol == null) throw new RuntimeException("Mala9inach l'colonne.");

            for (CSVRecord record : parser) {
                String valrStr = record.get(actualValrCol);
                String valr = valrStr != null ? valrStr.trim() : "";

                String dept = "INCONNU";
                if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                    String rawDept = record.get(actualDeptCol);
                    dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                }

                boolean isValr4 = valr.equals("4") || valr.equals("4.0") || valr.equals("4,0");
                boolean isValr5 = valr.equals("5") || valr.equals("5.0") || valr.equals("5,0");

                // GLOBAL
                statsMap.get("GLOBAL")[1]++;
                if (isValr4 || isValr5) statsMap.get("GLOBAL")[0]++;

                // DEPARTEMENT
                statsMap.putIfAbsent(dept, new long[]{0, 0});
                statsMap.get(dept)[1]++;
                if (isValr4 || isValr5) statsMap.get(dept)[0]++;
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SARCLI NOK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "SARCLI_NOK");

        Map<String, SarcliGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;
            double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

            finalDetails.put(dept, new SarcliGroupDto(num, denum, resultat, bonus));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("SARCLI_NOK").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(bonus).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return SarcliResultDto.builder().details(finalDetails).build();
    }

    private BonusConfigDto.SingleConfig parseSarcliConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getSarcli() != null) return fullConfig.getSarcli();
            } catch (Exception e) { log.error("Erreur parsing config SARCLI", e); }
        }
        return new BonusConfigDto.SingleConfig(30.0, 55.0, 1.0);
    }

    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}