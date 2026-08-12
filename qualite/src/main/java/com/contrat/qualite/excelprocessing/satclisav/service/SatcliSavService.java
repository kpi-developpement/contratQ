package com.contrat.qualite.excelprocessing.satclisav.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.satclisav.dto.SatcliSavGroupDto;
import com.contrat.qualite.excelprocessing.satclisav.dto.SatcliSavResultDto;
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
public class SatcliSavService {

    private final KpiArchiveRepository kpiArchiveRepository;

    public SatcliSavResultDto processSatcliSavExcel(MultipartFile file, int month, int year) {
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
            String flagInterCol = null;
            String volumeNoteCol = null;
            String actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("flag inter sans client")) flagInterCol = header;
                if (cleanHeader.contains("volume note")) volumeNoteCol = header;
                if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            if (flagInterCol == null || volumeNoteCol == null) {
                throw new RuntimeException("Mala9inach l'colonnes ('Flag Inter Sans Client' awla 'Volume Note').");
            }

            for (CSVRecord record : parser) {
                if (record.isMapped(flagInterCol) && record.isMapped(volumeNoteCol)) {
                    String rawFlag = record.get(flagInterCol);
                    String rawVol = record.get(volumeNoteCol);

                    String flagVal = rawFlag != null ? rawFlag.trim() : "";
                    String volVal = rawVol != null ? rawVol.trim() : "";

                    String dept = "INCONNU";
                    if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                        String rawDept = record.get(actualDeptCol);
                        dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                    }

                    boolean isFlag1 = flagVal.equals("1") || flagVal.equals("1.0") || flagVal.equals("1,0");
                    boolean isVol1 = volVal.equals("1") || volVal.equals("1.0") || volVal.equals("1,0");

                    if (isVol1) {
                        statsMap.get("GLOBAL")[1]++;
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[1]++;
                    }

                    if (isFlag1) {
                        statsMap.get("GLOBAL")[0]++;
                        statsMap.putIfAbsent(dept, new long[]{0, 0});
                        statsMap.get(dept)[0]++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SATCLI SAV)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessus(month, year, "SATCLI_SAV");

        Map<String, SatcliSavGroupDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            long num = entry.getValue()[0];
            long denum = entry.getValue()[1];

            double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

            finalDetails.put(dept, new SatcliSavGroupDto(num, denum, resultat, 0.0));

            archivesToSave.add(KpiArchive.builder()
                    .mois(month).annee(year).processus("SATCLI_SAV").departement(dept)
                    .num(num).denum(denum).resultat(resultat).partDeMarche(0.0).bonus(0.0).build());
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return SatcliSavResultDto.builder().details(finalDetails).build();
    }
}