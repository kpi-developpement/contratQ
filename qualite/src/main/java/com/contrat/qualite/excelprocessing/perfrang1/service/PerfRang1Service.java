package com.contrat.qualite.excelprocessing.perfrang1.service;

import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfGroupDto;
import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfRang1ResultDto;
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
public class PerfRang1Service {

    private static final String COL_ZONE = "Zone_statut prise";
    private static final String COL_RANG = "RANG_RDV (copie)";
    private static final String COL_STATUT = "GRP_STATUT_CRINSTALL_MNT";

    public PerfRang1ResultDto processPerfRang1Excel(MultipartFile file) {
        long numA = 0, denumA = 0;
        long numB = 0, denumB = 0;
        long numC = 0, denumC = 0;

        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            // Verification des colonnes
            if (!parser.getHeaderMap().containsKey(COL_ZONE) ||
                    !parser.getHeaderMap().containsKey(COL_RANG) ||
                    !parser.getHeaderMap().containsKey(COL_STATUT)) {
                throw new RuntimeException("Mala9inach wa7d mn les colonnes: " + COL_ZONE + ", " + COL_RANG + ", awla " + COL_STATUT);
            }

            for (CSVRecord record : parser) {
                // njibou les valeurs brutes
                String rawRang = record.get(COL_RANG);
                String rawZone = record.get(COL_ZONE);
                String rawStatut = record.get(COL_STATUT);

                // 1. CLEANUP DES VALEURS (Hada howa l'fix lkbir)
                // Kanhaydo l'espaces, w kanbedlo Retour a la ligne (\n wla \r) b Espace
                String rang = rawRang != null ? rawRang.trim() : "";

                String zone = rawZone != null ?
                        rawZone.toUpperCase().replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim()
                        : "";

                String statut = rawStatut != null ? rawStatut.toUpperCase().trim() : "";

                // 2. Les Conditions m9addin 100% kima glti
                // (RANG_RDV (copie) = 1) -> Y9der ykon "1", "1.0", awla "1,0" f Excel
                boolean isRang1 = rang.equals("1") || rang.equals("1.0") || rang.equals("1,0");

                // (Zone_statut prise = PLP ZONE X) -> Db 7it n9inaha, n9dro nkhdmo b equals
                boolean isZoneA = zone.equals("PLP ZONE A");
                boolean isZoneB = zone.equals("PLP ZONE B");
                boolean isZoneC = zone.equals("PLP ZONE C");

                // (GRP_STATUT_CRINSTALL_MNT = CR_MNT_OK)
                boolean isCrOk = statut.equals("CR_MNT_OK");

                // 3. APPLICATION DE LA REGLE DE CALCUL
                if (isRang1) {

                    // GROUP A
                    if (isZoneA) {
                        denumA++; // DENUM: Filtre = 1 & PLP ZONE A
                        if (isCrOk) {
                            numA++; // NUM: Filtre + CR_MNT_OK
                        }
                    }

                    // GROUP B
                    if (isZoneB) {
                        denumB++; // DENUM: Filtre = 1 & PLP ZONE B
                        if (isCrOk) {
                            numB++; // NUM: Filtre + CR_MNT_OK
                        }
                    }

                    // GROUP C
                    if (isZoneC) {
                        denumC++; // DENUM: Filtre = 1 & PLP ZONE C
                        if (isCrOk) {
                            numC++; // NUM: Filtre + CR_MNT_OK
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal fichier CSV (PERF RANG 1)", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        return PerfRang1ResultDto.builder()
                .groupA(buildGroupResult(numA, denumA))
                .groupB(buildGroupResult(numB, denumB))
                .groupC(buildGroupResult(numC, denumC))
                .build();
    }

    private PerfGroupDto buildGroupResult(long num, long denum) {
        double resultat = 0.0;
        if (denum > 0) {
            resultat = ((double) num / denum) * 100;
            resultat = Math.round(resultat * 100.0) / 100.0;
        }
        return PerfGroupDto.builder()
                .num(num)
                .denum(denum)
                .resultat(resultat)
                .build();
    }
}