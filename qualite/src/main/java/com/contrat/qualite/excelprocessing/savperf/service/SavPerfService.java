package com.contrat.qualite.excelprocessing.savperf.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.savperf.dto.SavPerfGroupDto;
import com.contrat.qualite.excelprocessing.savperf.dto.SavPerfResultDto;
import com.contrat.qualite.repository.KpiArchiveRepository;
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
public class SavPerfService {

    private final KpiArchiveRepository kpiArchiveRepository;

    public SavPerfResultDto processSavPerfExcel(MultipartFile file, int month, int year) {
        Map<String, long[]> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new long[]{0, 0});

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
            String statutCol = null;
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("statut intervention")) statutCol = header;
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            if (statutCol == null) throw new RuntimeException("Mala9inach l'colonne 'Statut Intervention'.");

            for (CSVRecord record : parser) {
                if (record.isMapped(statutCol)) {
                    String statutStr = record.get(statutCol);
                    String statut = statutStr != null ? statutStr.toUpperCase().trim() : "";

                    String dept = "INCONNU";
                    if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                        String rawDept = record.get(actualDeptCol);
                        dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                    }

                    if ("TERMINEE_OK".equals(statut)) {
                        statsMap.get("GLOBAL")[0]++;
                        statsMap.get("GLOBAL")[1]++;
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[0]++;
                        statsMap.get(dept)[1]++;
                    } else if ("TERMINEE_KO".equals(statut)) {
                        statsMap.get("GLOBAL")[1]++;
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[1]++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SAV PERF)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "SAV_PERF");

        Map<String, SavPerfGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

            finalDetails.put(dept, new SavPerfGroupDto(num, denum, resultat, 0.0));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("SAV_PERF").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(0.0).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return SavPerfResultDto.builder().details(finalDetails).build();
    }
}