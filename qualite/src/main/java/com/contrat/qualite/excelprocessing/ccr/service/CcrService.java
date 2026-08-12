package com.contrat.qualite.excelprocessing.ccr.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.ccr.dto.CcrGroupDto;
import com.contrat.qualite.excelprocessing.ccr.dto.CcrResultDto;
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
public class CcrService {

    private final KpiArchiveRepository kpiArchiveRepository;

    public CcrResultDto processCcrExcel(MultipartFile file, int month, int year) {
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
            String targetCol = null;
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("volume cr non exploitable") || cleanHeader.contains("cr non exploitable")) targetCol = header;
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne 'volume Cr non exploitable'");

            for (CSVRecord record : parser) {
                if (record.isMapped(targetCol)) {
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
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[1]++;

                        if (is1) {
                            statsMap.get("GLOBAL")[0]++;
                            statsMap.get(dept)[0]++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (CCR)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "CCR");

        Map<String, CcrGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

            finalDetails.put(dept, new CcrGroupDto(num, denum, resultat, 0.0));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("CCR").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(0.0).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return CcrResultDto.builder().details(finalDetails).build();
    }
}