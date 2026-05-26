package com.contrat.qualite.excelprocessing.sacli.service;

import com.contrat.qualite.excelprocessing.sacli.dto.SacliResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class SacliService {

    private static final String COL_STATUT = "GRP_STATUT_CRINSTALL_MNT";
    private static final String COL_VALR = "Valr Not Glbl";

    public SacliResultDto processSacliExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

        // Kan9raw l'fichier b BufferedReader (Naaaaaadi f les grands fichiers)
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 1. N9raw l'Header (Ligne lwla)
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new RuntimeException("L'fichier khawi awla mafihch Header");
            }

            // N9esmo l'header b point-virgule (;)
            String[] headers = headerLine.split(";");

            int statutColIndex = -1;
            int valrColIndex = -1;

            // 2. N9elbo 3la les indices dyal les colonnes
            for (int i = 0; i < headers.length; i++) {
                String headerName = headers[i].trim().replace("\"", ""); // N7iydo les guillemets ila kano
                if (headerName.equalsIgnoreCase(COL_STATUT)) {
                    statutColIndex = i;
                } else if (headerName.equalsIgnoreCase(COL_VALR)) {
                    valrColIndex = i;
                }
            }

            if (statutColIndex == -1 || valrColIndex == -1) {
                throw new RuntimeException("Mala9inach les colonnes mtloubin f l'fichier: " + COL_STATUT + " wla " + COL_VALR);
            }

            // 3. N9raw les lignes li b9aw whda b whda
            String line;
            while ((line = br.readLine()) != null) {
                // Split b point-virgule, l' -1 katkhli arrays b nfs taille wkha ykono les colonnes lkhrin khawyin
                String[] columns = line.split(";", -1);

                // N'évitiw les lignes li na9sin fihom les colonnes
                if (columns.length <= Math.max(statutColIndex, valrColIndex)) {
                    continue;
                }

                String statutValue = columns[statutColIndex].trim().replace("\"", "");
                String valrValue = columns[valrColIndex].trim().replace("\"", "");

                // 4. N'appliquiw l'logique
                if ("CR_MNT_OK".equalsIgnoreCase(statutValue)) {
                    denum++; // B. TOT. dyal CR_MNT_OK

                    if ("5".equals(valrValue) || "5.0".equals(valrValue)) {
                        num++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV", e);
            throw new RuntimeException("Erreur f l'analyse dyal fichier: " + e.getMessage());
        }

        // 5. Calcul dyal pourcentage
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }

        return SacliResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}