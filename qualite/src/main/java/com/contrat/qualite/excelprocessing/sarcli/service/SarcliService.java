package com.contrat.qualite.excelprocessing.sarcli.service;

import com.contrat.qualite.excelprocessing.sarcli.dto.SarcliResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class SarcliService {

    private static final String COL_STATUT = "GRP_STATUT_CRINSTALL_MNT";
    private static final String COL_VALR = "Valr Not Glbl";

    public SarcliResultDto processSarcliExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new RuntimeException("L'fichier khawi awla mafihch Header");
            }

            String[] headers = headerLine.split(";");
            int statutColIndex = -1;
            int valrColIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String headerName = headers[i].trim().replace("\"", "");
                if (headerName.equalsIgnoreCase(COL_STATUT)) {
                    statutColIndex = i;
                } else if (headerName.equalsIgnoreCase(COL_VALR)) {
                    valrColIndex = i;
                }
            }

            if (statutColIndex == -1 || valrColIndex == -1) {
                throw new RuntimeException("Mala9inach les colonnes mtloubin f l'fichier: " + COL_STATUT + " wla " + COL_VALR);
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";", -1);

                if (columns.length <= Math.max(statutColIndex, valrColIndex)) {
                    continue;
                }

                String statutValue = columns[statutColIndex].trim().replace("\"", "");
                String valrValue = columns[valrColIndex].trim().replace("\"", "");

                // L'LOGIQUE JDIDA DYAL SARCLI NOK
                if ("CR_MNT_NOK".equalsIgnoreCase(statutValue) || "CR_MNT_DELAI".equalsIgnoreCase(statutValue)) {
                    denum++; // B. TOT.

                    // N'vérifiw ila kant Valr Not Glbl = 4 awla 5
                    if ("4".equals(valrValue) || "4.0".equals(valrValue) ||
                            "5".equals(valrValue) || "5.0".equals(valrValue)) {
                        num++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (SARCLI NOK)", e);
            throw new RuntimeException("Erreur f l'analyse dyal fichier: " + e.getMessage());
        }

        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }

        return SarcliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}