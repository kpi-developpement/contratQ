package com.contrat.qualite.excelprocessing.zmdamii.service;

import com.contrat.qualite.excelprocessing.zmdamii.dto.ZmdAmiiResultDto;
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
public class ZmdAmiiService {

    public ZmdAmiiResultDto processZmdAmiiExcel(MultipartFile file) {
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
            String targetCol = null;

            // Nqelbou 3la l'colonne "Taux de report TH"
            for (String header : headerMap.keySet()) {
                if (header.trim().equalsIgnoreCase("Taux de report TH")) {
                    targetCol = header;
                    break;
                }
            }

            if (targetCol == null) {
                throw new RuntimeException("Mala9inach l'colonne 'Taux de report TH' f l'fichier ZMD AMII.");
            }

            // Calcul: Denum = (1 ou 0), Num = (1)
            for (CSVRecord record : parser) {
                if (record.isMapped(targetCol)) {
                    String rawVal = record.get(targetCol);
                    String val = rawVal != null ? rawVal.trim() : "";

                    boolean is1 = val.equals("1") || val.equals("1.0") || val.equals("1,0");
                    boolean is0 = val.equals("0") || val.equals("0.0") || val.equals("0,0");

                    if (is1 || is0) {
                        denum++;
                        if (is1) {
                            num++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (ZMD AMII)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return ZmdAmiiResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .partDeMarche(0.0)
                .bonus(0.0)
                .build();
    }
}