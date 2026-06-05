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
public class Fichier2AggregatorService {

    public Fichier2ResponseDto processFichier2(MultipartFile file) {
        // TNH
        long tnhNum = 0, tnhDenum = 0;
        // Perf Rang 1 (PLP)
        long p1NumA = 0, p1DenA = 0, p1NumB = 0, p1DenB = 0, p1NumC = 0, p1DenC = 0;
        // Hotline Rang 1
        long hNumA = 0, hDenA = 0, hNumB = 0, hDenB = 0, hNumC = 0, hDenC = 0;
        // Construction Rang 1
        long cNumA = 0, cDenA = 0, cNumB = 0, cDenB = 0, cNumC = 0, cDenC = 0;
        // Perf Rang 2
        long p2NumA = 0, p2DenA = 0, p2NumB = 0, p2DenB = 0, p2NumC = 0, p2DenC = 0;

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
            String actualZoneCol = null, actualRangCol = null, actualStatutCol = null, actualMotifCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("zone_statut prise")) actualZoneCol = header;
                else if (cleanHeader.contains("rang_rdv") && cleanHeader.contains("(copie)")) actualRangCol = header;
                else if (cleanHeader.contains("grp_statut_crinstall_mnt")) actualStatutCol = header;
                else if (cleanHeader.contains("motf_ko") || cleanHeader.contains("motif_ko")) actualMotifCol = header;
            }

            for (CSVRecord record : parser) {
                // 1. TNH
                tnhDenum++;
                if (actualMotifCol != null && record.isMapped(actualMotifCol)) {
                    String motifValue = record.get(actualMotifCol);
                    if (motifValue != null && "CR DELAI - Organisation installateur".equalsIgnoreCase(motifValue.trim())) {
                        tnhNum++;
                    }
                }

                // 2. RANG 1 W RANG 2
                if (actualRangCol != null && actualZoneCol != null && actualStatutCol != null
                        && record.isMapped(actualRangCol) && record.isMapped(actualZoneCol) && record.isMapped(actualStatutCol)) {

                    String rawRang = record.get(actualRangCol);
                    String rawZone = record.get(actualZoneCol);
                    String rawStatut = record.get(actualStatutCol);

                    String rang = rawRang != null ? rawRang.trim() : "";
                    String zone = rawZone != null ? rawZone.toUpperCase().replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim() : "";
                    String statut = rawStatut != null ? rawStatut.toUpperCase().trim() : "";

                    boolean isRang1 = rang.equals("1") || rang.equals("1.0") || rang.equals("1,0");
                    boolean isCrOk = statut.equals("CR_MNT_OK");

                    if (isRang1) {
                        if (zone.equals("PLP ZONE A")) { p1DenA++; if(isCrOk) p1NumA++; }
                        if (zone.equals("PLP ZONE B")) { p1DenB++; if(isCrOk) p1NumB++; }
                        if (zone.equals("PLP ZONE C")) { p1DenC++; if(isCrOk) p1NumC++; }

                        if (zone.equals("HOTLINE ZONE A")) { hDenA++; if(isCrOk) hNumA++; }
                        if (zone.equals("HOTLINE ZONE B")) { hDenB++; if(isCrOk) hNumB++; }
                        if (zone.equals("HOTLINE ZONE C")) { hDenC++; if(isCrOk) hNumC++; }

                        if (zone.equals("CONSTRUCTION ZONE A")) { cDenA++; if(isCrOk) cNumA++; }
                        if (zone.equals("CONSTRUCTION ZONE B")) { cDenB++; if(isCrOk) cNumB++; }
                        if (zone.equals("CONSTRUCTION ZONE C")) { cDenC++; if(isCrOk) cNumC++; }
                    }

                    boolean isRangNot1 = !rang.isEmpty() && !isRang1;
                    if (isRangNot1) {
                        if (zone.contains("ZONE A")) { p2DenA++; if(isCrOk) p2NumA++; }
                        if (zone.contains("ZONE B")) { p2DenB++; if(isCrOk) p2NumB++; }
                        if (zone.contains("ZONE C")) { p2DenC++; if(isCrOk) p2NumC++; }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal Fichier 2", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        // ==========================================
        // L'INTELLIGENCE JDIDA: Calcul dyal Total Denums
        // ==========================================

        // Jme3na ga3 les Denum dyal Rang 1 (PLP + Hotline + Construct) f A w B w C
        long totalDenumRang1 = p1DenA + p1DenB + p1DenC +
                hDenA + hDenB + hDenC +
                cDenA + cDenB + cDenC;

        // Jme3na ga3 les Denum dyal Rang 2
        long totalDenumRang2 = p2DenA + p2DenB + p2DenC;

        return Fichier2ResponseDto.builder()
                .tnh(new TnhResultDto(tnhNum, tnhDenum, calc(tnhNum, tnhDenum)))

                .perfRang1(new PerfRang1ResultDto(
                        new PerfGroupDto(p1NumA, p1DenA, calc(p1NumA, p1DenA), calcPart(p1DenA, totalDenumRang1)),
                        new PerfGroupDto(p1NumB, p1DenB, calc(p1NumB, p1DenB), calcPart(p1DenB, totalDenumRang1)),
                        new PerfGroupDto(p1NumC, p1DenC, calc(p1NumC, p1DenC), calcPart(p1DenC, totalDenumRang1))
                ))

                .hotlineRang1(new HotlineRang1ResultDto(
                        new HotlineGroupDto(hNumA, hDenA, calc(hNumA, hDenA), calcPart(hDenA, totalDenumRang1)),
                        new HotlineGroupDto(hNumB, hDenB, calc(hNumB, hDenB), calcPart(hDenB, totalDenumRang1)),
                        new HotlineGroupDto(hNumC, hDenC, calc(hNumC, hDenC), calcPart(hDenC, totalDenumRang1))
                ))

                .constructionRang1(new ConstructionRang1ResultDto(
                        new ConstructionGroupDto(cNumA, cDenA, calc(cNumA, cDenA), calcPart(cDenA, totalDenumRang1)),
                        new ConstructionGroupDto(cNumB, cDenB, calc(cNumB, cDenB), calcPart(cDenB, totalDenumRang1)),
                        new ConstructionGroupDto(cNumC, cDenC, calc(cNumC, cDenC), calcPart(cDenC, totalDenumRang1))
                ))

                .perfRang2(new PerfRang2ResultDto(
                        new PerfRang2GroupDto(p2NumA, p2DenA, calc(p2NumA, p2DenA), calcPart(p2DenA, totalDenumRang2)),
                        new PerfRang2GroupDto(p2NumB, p2DenB, calc(p2NumB, p2DenB), calcPart(p2DenB, totalDenumRang2)),
                        new PerfRang2GroupDto(p2NumC, p2DenC, calc(p2NumC, p2DenC), calcPart(p2DenC, totalDenumRang2))
                ))
                .build();
    }

    private double calc(long num, long denum) {
        if (denum == 0) return 0.0;
        return Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
    }

    // Fonction jdida l'calcul dyal Part de Marché
    private double calcPart(long localDenum, long globalDenum) {
        if (globalDenum == 0) return 0.0;
        return Math.round((((double) localDenum / globalDenum) * 100) * 100.0) / 100.0;
    }
}