package com.contrat.qualite.excelprocessing.satclisav.service;

import com.contrat.qualite.excelprocessing.satclisav.dto.SatcliSavResultDto;
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
public class SatcliSavService {

    public SatcliSavResultDto processSatcliSavExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

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

            // Fuzzy match bach njbdou les 2 colonnes s7a7
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("flag inter sans client")) flagInterCol = header;
                if (cleanHeader.contains("volume note")) volumeNoteCol = header;
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

                    boolean isFlag1 = flagVal.equals("1") || flagVal.equals("1.0") || flagVal.equals("1,0");
                    boolean isVol1 = volVal.equals("1") || volVal.equals("1.0") || volVal.equals("1,0");

                    // L'Logic dyal l'Calcul:
                    // DENUM = ay ligne fiha 1 f Volume Note
                    if (isVol1) {
                        denum++;
                    }

                    // NUM = ay ligne fiha 1 f Flag Inter Sans Client
                    if (isFlag1) {
                        num++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SATCLI SAV)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return SatcliSavResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(0.0)
                .build();
    }
}