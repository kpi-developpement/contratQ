package com.contrat.qualite.excelprocessing.securisation.service;

import com.contrat.qualite.excelprocessing.securisation.dto.SecurisationResultDto;
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
public class SecurisationService {

    public SecurisationResultDto processSecurisationExcel(MultipartFile file) {
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

            // Fuzzy Match bach y9bet l'colonne wakha ykoun fiha espace zayd awla l'3am ytbeddel
            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("flag secu interv")) {
                    targetCol = header;
                    break;
                }
            }

            if (targetCol == null) throw new RuntimeException("Mala9inach l'colonne 'Flag Secu Interv'");

            for (CSVRecord record : parser) {
                if (record.isMapped(targetCol)) {
                    String rawVal = record.get(targetCol);
                    String val = rawVal != null ? rawVal.trim() : "";

                    boolean is1 = val.equals("1") || val.equals("1.0") || val.equals("1,0");
                    boolean is0 = val.equals("0") || val.equals("0.0") || val.equals("0,0");

                    // L'Logic dyal l'Calcul: (1 + 0) l DENUM, w (1) bo7do l NUM
                    if (is1 || is0) {
                        denum++;
                        if (is1) num++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (Sécurisation)", e);
            throw new RuntimeException("Erreur: " + e.getMessage());
        }

        double resultat = denum > 0 ? Math.round((((double) num / denum) * 100) * 100.0) / 100.0 : 0.0;

        return SecurisationResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .bonus(0.0)
                .build();
    }
}