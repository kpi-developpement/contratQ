package com.contrat.qualite.excelprocessing.savdelai.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.savdelai.dto.SavDelaiGroupDto;
import com.contrat.qualite.excelprocessing.savdelai.dto.SavDelaiResultDto;
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
public class SavDelaiService {

    private final KpiArchiveRepository kpiArchiveRepository;

    public SavDelaiResultDto processSavDelaiExcel(MultipartFile file, int month, int year) {
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
            String paretoCol = null;
            String dateRdvCol = null;
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim();
                if (cleanHeader.equalsIgnoreCase("Pareto Delai RDV")) paretoCol = header;
                if (cleanHeader.equalsIgnoreCase("Date de RDV client")) dateRdvCol = header;
                if (cleanHeader.toLowerCase().contains("code dep") || cleanHeader.toLowerCase().contains("departement") || cleanHeader.toLowerCase().contains("dpt")) actualDeptCol = header;
            }

            if (paretoCol == null || dateRdvCol == null) {
                throw new RuntimeException("Mala9inach l'colonnes mzyan.");
            }

            for (CSVRecord record : parser) {
                String dept = "INCONNU";
                if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                    String rawDept = record.get(actualDeptCol);
                    dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                }

                boolean hasDate = false;
                if (record.isMapped(dateRdvCol)) {
                    String dateRdvVal = record.get(dateRdvCol);
                    if (dateRdvVal != null && !dateRdvVal.trim().isEmpty()) {
                        hasDate = true;
                        statsMap.get("GLOBAL")[1]++;
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[1]++;
                    }
                }

                if (hasDate && record.isMapped(paretoCol)) {
                    String paretoVal = record.get(paretoCol);
                    if (paretoVal != null && paretoVal.trim().equalsIgnoreCase("0-3j")) {
                        statsMap.get("GLOBAL")[0]++;
                        statsMap.get(dept)[0]++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SAV DELAI)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "SAV_DELAI");

        Map<String, SavDelaiGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

            finalDetails.put(dept, new SavDelaiGroupDto(num, denum, resultat, 0.0));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("SAV_DELAI").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(0.0).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return SavDelaiResultDto.builder().details(finalDetails).build();
    }
}