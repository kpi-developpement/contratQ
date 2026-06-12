package com.contrat.qualite.excelprocessing.savdelai.service;

import com.contrat.qualite.excelprocessing.savdelai.dto.SavDelaiResultDto;
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
public class SavDelaiService {

    public SavDelaiResultDto processSavDelaiExcel(MultipartFile file) {
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
            String paretoCol = null;
            String dateRdvCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("pareto delai rdv")) paretoCol = header;
                if (cleanHeader.contains("date de rdv client") || cleanHeader.contains("date rdv client")) dateRdvCol = header;
            }

            if (paretoCol == null || dateRdvCol == null) {
                throw new RuntimeException("Mala9inach l'colonnes mzyan (Pareto Delai RDV wla Date de RDV client).");
            }

            for (CSVRecord record : parser) {
                if (record.isMapped(paretoCol) && record.isMapped(dateRdvCol)) {
                    String paretoVal = record.get(paretoCol) != null ? record.get(paretoCol).trim().toLowerCase() : "";
                    String dateRdvVal = record.get(dateRdvCol) != null ? record.get(dateRdvCol).trim() : "";

                    // L'Logic dyal l'Calcul (DENUM: lignes li fihom Date RDV m3emra)
                    if (!dateRdvVal.isEmpty()) {
                        denum++;
                        // NUM: Lignes li fihom 0-3j f Pareto
                        if (paretoVal.equals("0-3j") || paretoVal.equals("0-3 j") || paretoVal.equals("0-3")) {
                            num++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SAV DELAI)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return SavDelaiResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(0.0)
                .build();
    }
}