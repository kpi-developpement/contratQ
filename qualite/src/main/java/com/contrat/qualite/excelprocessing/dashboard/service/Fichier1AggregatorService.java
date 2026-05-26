package com.contrat.qualite.excelprocessing.dashboard.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier1ResponseDto;
import com.contrat.qualite.excelprocessing.sacli.dto.SacliResultDto;
import com.contrat.qualite.excelprocessing.sarcli.dto.SarcliResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class Fichier1AggregatorService {

    private static final String COL_STATUT = "GRP_STATUT_CRINSTALL_MNT";
    private static final String COL_VALR = "Valr Not Glbl";

    public Fichier1ResponseDto processFichier1(MultipartFile file) {
        long sacliNum = 0, sacliDenum = 0;
        long sarcliNum = 0, sarcliDenum = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new RuntimeException("Fichier 1 khawi");

            String[] headers = headerLine.split(";");
            int statutColIndex = -1;
            int valrColIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String headerName = headers[i].trim().replace("\"", "");
                if (headerName.equalsIgnoreCase(COL_STATUT)) statutColIndex = i;
                else if (headerName.equalsIgnoreCase(COL_VALR)) valrColIndex = i;
            }

            if (statutColIndex == -1 || valrColIndex == -1) {
                throw new RuntimeException("Mala9inach les colonnes mtloubin f Fichier 1");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";", -1);
                if (columns.length <= Math.max(statutColIndex, valrColIndex)) continue;

                String statutValue = columns[statutColIndex].trim().replace("\"", "");
                String valrValue = columns[valrColIndex].trim().replace("\"", "");

                // --- LOGIQUE SACLI ---
                if ("CR_MNT_OK".equalsIgnoreCase(statutValue)) {
                    sacliDenum++;
                    if ("5".equals(valrValue) || "5.0".equals(valrValue)) sacliNum++;
                }
                // --- LOGIQUE SARCLI ---
                else if ("CR_MNT_NOK".equalsIgnoreCase(statutValue) || "CR_MNT_DELAI".equalsIgnoreCase(statutValue)) {
                    sarcliDenum++;
                    if ("4".equals(valrValue) || "4.0".equals(valrValue) || "5".equals(valrValue) || "5.0".equals(valrValue)) sarcliNum++;
                }
            }
        } catch (Exception e) {
            log.error("Erreur f Fichier 1", e);
            throw new RuntimeException("Erreur f l'analyse dyal Fichier 1: " + e.getMessage());
        }

        return Fichier1ResponseDto.builder()
                .sacli(new SacliResultDto(sacliNum, sacliDenum, calculatePourcentage(sacliNum, sacliDenum)))
                .sarcli(new SarcliResultDto(sarcliNum, sarcliDenum, calculatePourcentage(sarcliNum, sarcliDenum)))
                .build();
    }

    private double calculatePourcentage(long num, long denum) {
        if (denum == 0) return 0.0;
        return Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
    }
}