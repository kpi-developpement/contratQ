package com.contrat.qualite.excelprocessing.dashboard.service;

import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier2ResponseDto;
import com.contrat.qualite.excelprocessing.tnh.dto.TnhResultDto;
import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfRang1ResultDto;
import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfGroupDto;
import com.contrat.qualite.excelprocessing.hotlinerang1.dto.HotlineRang1ResultDto;
import com.contrat.qualite.excelprocessing.hotlinerang1.dto.HotlineGroupDto;
import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionRang1ResultDto;
import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionGroupDto;
import com.contrat.qualite.excelprocessing.perfrang2.dto.PerfRang2ResultDto;
import com.contrat.qualite.excelprocessing.perfrang2.dto.PerfRang2GroupDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class Fichier2AggregatorService {

    // L'noms mkhltin, nqelbou 3lihom b contains/ignoreCase
    private static final String COL_TNH = "motf_ko_cr_inst_first_crinstall_mnt";
    private static final String COL_ZONE = "zone_statut prise";
    private static final String COL_RANG = "rang_rdv (copie)";
    private static final String COL_STATUT = "grp_statut_crinstall_mnt";

    public Fichier2ResponseDto processFichier2(MultipartFile file) {
        // TNH
        long tnhNum = 0, tnhDenum = 0;
        // Perf Rang 1
        long p1NumA = 0, p1DenA = 0, p1NumB = 0, p1DenB = 0, p1NumC = 0, p1DenC = 0;
        // Hotline Rang 1
        long hNumA = 0, hDenA = 0, hNumB = 0, hDenB = 0, hNumC = 0, hDenC = 0;
        // Construction Rang 1
        long cNumA = 0, cDenA = 0, cNumB = 0, cDenB = 0, cNumC = 0, cDenC = 0;
        // Perf Rang 2
        long p2NumA = 0, p2DenA = 0, p2NumB = 0, p2DenB = 0, p2NumC = 0, p2DenC = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new RuntimeException("Fichier 2 khawi");

            String[] headers = headerLine.split(";", -1);
            int idxTnh = -1, idxZone = -1, idxRang = -1, idxStatut = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase().replace("\"", "");
                if (h.contains(COL_TNH)) idxTnh = i;
                else if (h.contains(COL_ZONE)) idxZone = i;
                else if (h.contains("rang_rdv") && h.contains("(copie)")) idxRang = i;
                else if (h.contains(COL_STATUT)) idxStatut = i;
            }

            if (idxTnh == -1 || idxZone == -1 || idxRang == -1 || idxStatut == -1) {
                throw new RuntimeException("Naqsin des colonnes f Fichier 2. Verifier: ZONE, RANG, STATUT awla MOTIF_KO");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";", -1);

                // 1. Logique TNH (khassha t-tchecka wkha ykoun array sghir ila kano l'columns mt9arbin)
                if (cols.length > idxTnh) {
                    String tnhVal = cols[idxTnh].trim().replace("\"", "");
                    if (!tnhVal.isEmpty()) {
                        tnhDenum++;
                        if ("CR DELAI - Organisation installateur".equalsIgnoreCase(tnhVal)) {
                            tnhNum++;
                        }
                    }
                }

                // 2. Logique les KPIs lokhrin (Zone, Rang, Statut)
                if (cols.length > Math.max(idxZone, Math.max(idxRang, idxStatut))) {
                    String rawZone = cols[idxZone].trim().replace("\"", "");
                    String rawRang = cols[idxRang].trim().replace("\"", "");
                    String rawStatut = cols[idxStatut].trim().replace("\"", "");

                    String zone = rawZone.toUpperCase().replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim();
                    String statut = rawStatut.toUpperCase().trim();
                    boolean isCrOk = "CR_MNT_OK".equals(statut);

                    boolean isRang1 = rawRang.equals("1") || rawRang.equals("1.0") || rawRang.equals("1,0");
                    boolean isRangNot1 = !rawRang.isEmpty() && !isRang1;

                    // PERF RANG 1 (PLP)
                    if (isRang1 && zone.startsWith("PLP ZONE")) {
                        if (zone.contains("ZONE A")) { p1DenA++; if (isCrOk) p1NumA++; }
                        else if (zone.contains("ZONE B")) { p1DenB++; if (isCrOk) p1NumB++; }
                        else if (zone.contains("ZONE C")) { p1DenC++; if (isCrOk) p1NumC++; }
                    }
                    // HOTLINE RANG 1
                    else if (isRang1 && zone.startsWith("HOTLINE ZONE")) {
                        if (zone.contains("ZONE A")) { hDenA++; if (isCrOk) hNumA++; }
                        else if (zone.contains("ZONE B")) { hDenB++; if (isCrOk) hNumB++; }
                        else if (zone.contains("ZONE C")) { hDenC++; if (isCrOk) hNumC++; }
                    }
                    // CONSTRUCTION RANG 1
                    else if (isRang1 && zone.startsWith("CONSTRUCTION ZONE")) {
                        if (zone.contains("ZONE A")) { cDenA++; if (isCrOk) cNumA++; }
                        else if (zone.contains("ZONE B")) { cDenB++; if (isCrOk) cNumB++; }
                        else if (zone.contains("ZONE C")) { cDenC++; if (isCrOk) cNumC++; }
                    }
                    // PERF RANG 2 (Ga3 les zones machi 1)
                    if (isRangNot1) {
                        if (zone.contains("ZONE A")) { p2DenA++; if (isCrOk) p2NumA++; }
                        else if (zone.contains("ZONE B")) { p2DenB++; if (isCrOk) p2NumB++; }
                        else if (zone.contains("ZONE C")) { p2DenC++; if (isCrOk) p2NumC++; }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal Fichier 2", e);
            throw new RuntimeException("L'fichier 2 fih mochkil awla format mbedel.");
        }

        return Fichier2ResponseDto.builder()
                .tnh(new TnhResultDto(tnhNum, tnhDenum, calc(tnhNum, tnhDenum)))
                .perfRang1(new PerfRang1ResultDto(
                        new PerfGroupDto(p1NumA, p1DenA, calc(p1NumA, p1DenA)),
                        new PerfGroupDto(p1NumB, p1DenB, calc(p1NumB, p1DenB)),
                        new PerfGroupDto(p1NumC, p1DenC, calc(p1NumC, p1DenC))
                ))
                .hotlineRang1(new HotlineRang1ResultDto(
                        new HotlineGroupDto(hNumA, hDenA, calc(hNumA, hDenA)),
                        new HotlineGroupDto(hNumB, hDenB, calc(hNumB, hDenB)),
                        new HotlineGroupDto(hNumC, hDenC, calc(hNumC, hDenC))
                ))
                .constructionRang1(new ConstructionRang1ResultDto(
                        new ConstructionGroupDto(cNumA, cDenA, calc(cNumA, cDenA)),
                        new ConstructionGroupDto(cNumB, cDenB, calc(cNumB, cDenB)),
                        new ConstructionGroupDto(cNumC, cDenC, calc(cNumC, cDenC))
                ))
                .perfRang2(new PerfRang2ResultDto(
                        new PerfRang2GroupDto(p2NumA, p2DenA, calc(p2NumA, p2DenA)),
                        new PerfRang2GroupDto(p2NumB, p2DenB, calc(p2NumB, p2DenB)),
                        new PerfRang2GroupDto(p2NumC, p2DenC, calc(p2NumC, p2DenC))
                ))
                .build();
    }

    private double calc(long num, long denum) {
        if (denum == 0) return 0.0;
        return Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
    }
}