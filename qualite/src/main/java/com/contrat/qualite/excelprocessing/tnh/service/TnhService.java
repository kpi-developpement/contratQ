package com.contrat.qualite.excelprocessing.tnh.service;

import com.contrat.qualite.excelprocessing.tnh.dto.TnhResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class TnhService {

    private static final String COL_MOTIF = "MOTF_KO_CR_INST_FIRST_CRINSTALL_MNT";
    private static final String EXPECTED_VAL = "CR DELAI - Organisation installateur";

    public TnhResultDto processTnhExcel(MultipartFile file) {
        long num = 0;
        long denum = 0;

        // Configuration dyal Parser: kay3ref l'Header, kayfhem les guillemets ("") w l'point-virgule (;)
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader() // Kay9ra l'ligne lwla b7al headers
                .setSkipHeaderRecord(true) // May7sebch l'header f DENUM
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            // Vérification wach l'colonne kayna bse7
            if (!parser.getHeaderMap().containsKey(COL_MOTIF)) {
                throw new RuntimeException("Mala9inach l'colonne mtlouba f l'fichier: " + COL_MOTIF);
            }

            for (CSVRecord record : parser) {
                denum++; // Kay7seb ga3 les lignes s7a7 (bla header)

                String motifValue = record.get(COL_MOTIF);

                // N'appliquiw l'logique s7i7a b ignoreCase bach ntfadaw maj/min
                if (motifValue != null && EXPECTED_VAL.equalsIgnoreCase(motifValue.trim())) {
                    num++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (TNH)", e);
            throw new RuntimeException("Erreur f l'analyse dyal fichier TNH: " + e.getMessage());
        }

        // Division w Pourcentage
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            // Arrondir l 2 chiffres mor l'fasila
            resultat = Math.round(resultat * 100.0) / 100.0;
        }

        return TnhResultDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}