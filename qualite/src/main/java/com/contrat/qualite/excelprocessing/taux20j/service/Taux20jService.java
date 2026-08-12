package com.contrat.qualite.excelprocessing.taux20j.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.taux20j.dto.Taux20jGroupDto;
import com.contrat.qualite.excelprocessing.taux20j.dto.Taux20jResultDto;
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
public class Taux20jService {

    private final KpiArchiveRepository kpiArchiveRepository;

    public Taux20jResultDto processTaux20jExcel(MultipartFile file, String configJson, int month, int year) {
        Map<String, long[]> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new long[]{0, 0});

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
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("inf 20") && cleanHeader.contains("rdv")) targetCol = header;
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne Volume de cmd SS-RDV inf 20");

            for (CSVRecord record : parser) {
                String rawVal = record.get(targetCol);
                String val = rawVal != null ? rawVal.trim() : "";

                String dept = "INCONNU";
                if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                    String rawDept = record.get(actualDeptCol);
                    dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                }

                boolean is1 = val.equals("1") || val.equals("1.0") || val.equals("1,0");
                boolean is0 = val.equals("0") || val.equals("0.0") || val.equals("0,0");

                if (is1 || is0) {
                    statsMap.get("GLOBAL")[1]++;
                    if (is1) statsMap.get("GLOBAL")[0]++;

                    statsMap.putIfAbsent(dept, new long[]{0, 0});
                    statsMap.get(dept)[1]++;
                    if (is1) statsMap.get(dept)[0]++;
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (Taux 20J)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "TAUX_20J");

        Map<String, Taux20jGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;
            double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

            finalDetails.put(dept, new Taux20jGroupDto(num, denum, resultat, bonus));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("TAUX_20J").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(bonus).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return Taux20jResultDto.builder().details(finalDetails).build();
    }

    private BonusConfigDto.SingleConfig parseTaux20jConfig(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BonusConfigDto fullConfig = mapper.readValue(configJson, BonusConfigDto.class);
                if (fullConfig.getTaux20j() != null) return fullConfig.getTaux20j();
            } catch (Exception e) { log.error("Erreur parsing config TAUX 20J", e); }
        }
        return new BonusConfigDto.SingleConfig(80.0, 95.0, 2.0);
    }

    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}