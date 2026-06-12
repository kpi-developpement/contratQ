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

            // Nqelbou 3la les noms dyal les colonnes f l'Header
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("pareto delai rdv")) paretoCol = header;
                if (cleanHeader.contains("date de rdv client") || cleanHeader.contains("date rdv client")) dateRdvCol = header;
            }

            if (paretoCol == null || dateRdvCol == null) {
                throw new RuntimeException("Mala9inach l'colonnes mzyan (Pareto Delai RDV wla Date de RDV client).");
            }

            for (CSVRecord record : parser) {

                // 1. CALCUL DYAL NUM (M3ZOOL) - L'Valeur exacte "0-3j"
                if (record.isMapped(paretoCol)) {
                    String paretoVal = record.get(paretoCol);
                    // Kan-testiw wach l'valeur katsawi b debt "0-3j" (Ignore Case)
                    if (paretoVal != null && paretoVal.trim().equalsIgnoreCase("0-3j")) {
                        num++;
                    }
                }

                // 2. CALCUL DYAL DENUM (M3ZOOL) - Date de RDV m3emra
                if (record.isMapped(dateRdvCol)) {
                    String dateRdvVal = record.get(dateRdvCol);
                    if (dateRdvVal != null && !dateRdvVal.trim().isEmpty()) {
                        denum++;
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