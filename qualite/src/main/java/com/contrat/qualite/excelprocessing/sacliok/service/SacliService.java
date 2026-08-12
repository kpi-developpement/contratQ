package com.contrat.qualite.excelprocessing.sacliok.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.sacliok.dto.SacliGroupDto;
import com.contrat.qualite.excelprocessing.sacliok.dto.SacliResultDto;
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
public class SacliService {

    private static final String EXPECTED_VALR = "valr not glbl";
    private final KpiArchiveRepository kpiArchiveRepository;

    public SacliResultDto processSacliExcel(MultipartFile file, String configJson, int month, int year) {

        // Map bach n-stockiw les calculs: Key = Departement (awla "GLOBAL")
        Map<String, long[]> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new long[]{0, 0}); // Index 0 = num, Index 1 = denum

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
            String actualDeptCol = null;

            // Nqelbou 3la l'colonne dyal l'valeur w dyal l'département
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains(EXPECTED_VALR)) {
                    actualValrCol = header;
                }
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) {
                    actualDeptCol = header;
                }
            }

            if (actualValrCol == null) throw new RuntimeException("Mala9inach l'colonne 'valr not glbl'.");
            if (actualDeptCol == null) log.warn("Mala9inach l'colonne 'Code Departement'. L'calcul ghaydar ghir GLOBAL.");

            for (CSVRecord record : parser) {
                String valrStr = record.get(actualValrCol);
                String valr = valrStr != null ? valrStr.trim() : "";

                String dept = "INCONNU";
                if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                    String rawDept = record.get(actualDeptCol);
                    dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                }

                boolean isNum = valr.equals("5") || valr.equals("5.0") || valr.equals("5,0");

                // 1. Update GLOBAL
                statsMap.get("GLOBAL")[1]++; // Denum
                if (isNum) statsMap.get("GLOBAL")[0]++; // Num

                // 2. Update Département
                statsMap.putIfAbsent(dept, new long[]{0, 0});
                statsMap.get(dept)[1]++;
                if (isNum) statsMap.get(dept)[0]++;
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SACLI OK)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        // Nettoyage dyal DB 9bel ma n-sauvegardiw jdid
        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "SACLI_OK");

        Map<String, SacliGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        // Calcul final w préparation l'sauvegarde
        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;
            double bonus = calcPureBonus(resultat, config.getMin(), config.getMax(), config.getBonusMax());

            SacliGroupDto dto = new SacliGroupDto(num, denum, resultat, bonus);
            finalDetails.put(dept, dto);

            // Création dyal l'Archive
            archivesToSave.add(KpiArchive.builder()
                    .mois(month)
                    .annee(year)
                    .processus("SACLI_OK")
                    .departement(dept)
                    .num(num)
                    .denum(denum)
                    .resultat(resultat)
                    .partDeMarche(0.0) // Isolé
                    .bonus(bonus)
                    .build());
        }

        // Sauvegarde f l'Base de données (10.10.10.50)
        kpiArchiveRepository.saveAll(archivesToSave);

        return SacliResultDto.builder()
                .details(finalDetails)
                .build();
    }

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
        return new BonusConfigDto.SingleConfig(85.0, 95.0, 2.0);
    }

    private double calcPureBonus(double resultat, double pointMin, double pointMax, double bonusMax) {
        if (resultat <= pointMin) return 0.0;
        if (resultat >= pointMax) return bonusMax;
        double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax;
        return Math.round(bonus * 100.0) / 100.0;
    }
}