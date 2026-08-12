package com.contrat.qualite.excelprocessing.dashboard.service;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.excelprocessing.dashboard.dto.BonusConfigDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier2DeptDataDto;
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
import com.contrat.qualite.repository.KpiArchiveRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class Fichier2AggregatorService {

    private final KpiArchiveRepository kpiArchiveRepository;

    private static class RawStats {
        long tnhNum = 0, tnhDenum = 0;
        long p1NumA = 0, p1DenA = 0, p1NumB = 0, p1DenB = 0, p1NumC = 0, p1DenC = 0;
        long hNumA = 0, hDenA = 0, hNumB = 0, hDenB = 0, hNumC = 0, hDenC = 0;
        long cNumA = 0, cDenA = 0, cNumB = 0, cDenB = 0, cNumC = 0, cDenC = 0;
        long p2NumA = 0, p2DenA = 0, p2NumB = 0, p2DenB = 0, p2NumC = 0, p2DenC = 0;
    }

    public Fichier2ResponseDto processFichier2(MultipartFile file, String configJson, int month, int year) {
        Map<String, RawStats> statsMap = new HashMap<>();
        statsMap.put("GLOBAL", new RawStats());

        BonusConfigDto config = parseConfigOrDefault(configJson);

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
            String actualZoneCol = null, actualRangCol = null, actualStatutCol = null, actualMotifCol = null, actualDeptCol = null;

            for (String header : headerMap.keySet()) {
                String cleanHeader = header.trim().toLowerCase();
                if (cleanHeader.contains("zone_statut prise")) actualZoneCol = header;
                else if (cleanHeader.contains("rang_rdv") && cleanHeader.contains("(copie)")) actualRangCol = header;
                else if (cleanHeader.contains("grp_statut_crinstall_mnt")) actualStatutCol = header;
                else if (cleanHeader.contains("motf_ko") || cleanHeader.contains("motif_ko")) actualMotifCol = header;
                else if (cleanHeader.contains("code dep") || cleanHeader.contains("departement") || cleanHeader.contains("dpt")) actualDeptCol = header;
            }

            for (CSVRecord record : parser) {
                String dept = "INCONNU";
                if (actualDeptCol != null && record.isMapped(actualDeptCol)) {
                    String rawDept = record.get(actualDeptCol);
                    dept = rawDept != null && !rawDept.trim().isEmpty() ? rawDept.trim() : "INCONNU";
                }

                statsMap.putIfAbsent(dept, new RawStats());
                RawStats global = statsMap.get("GLOBAL");
                RawStats local = statsMap.get(dept);

                // TNH
                global.tnhDenum++;
                local.tnhDenum++;
                if (actualMotifCol != null && record.isMapped(actualMotifCol)) {
                    String motifValue = record.get(actualMotifCol);
                    if (motifValue != null && "CR DELAI - Organisation installateur".equalsIgnoreCase(motifValue.trim())) {
                        global.tnhNum++;
                        local.tnhNum++;
                    }
                }

                // RANG 1 & 2
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
                        if (zone.equals("PLP ZONE A")) { global.p1DenA++; local.p1DenA++; if(isCrOk) { global.p1NumA++; local.p1NumA++; } }
                        if (zone.equals("PLP ZONE B")) { global.p1DenB++; local.p1DenB++; if(isCrOk) { global.p1NumB++; local.p1NumB++; } }
                        if (zone.equals("PLP ZONE C")) { global.p1DenC++; local.p1DenC++; if(isCrOk) { global.p1NumC++; local.p1NumC++; } }

                        if (zone.equals("HOTLINE ZONE A")) { global.hDenA++; local.hDenA++; if(isCrOk) { global.hNumA++; local.hNumA++; } }
                        if (zone.equals("HOTLINE ZONE B")) { global.hDenB++; local.hDenB++; if(isCrOk) { global.hNumB++; local.hNumB++; } }
                        if (zone.equals("HOTLINE ZONE C")) { global.hDenC++; local.hDenC++; if(isCrOk) { global.hNumC++; local.hNumC++; } }

                        if (zone.equals("CONSTRUCTION ZONE A")) { global.cDenA++; local.cDenA++; if(isCrOk) { global.cNumA++; local.cNumA++; } }
                        if (zone.equals("CONSTRUCTION ZONE B")) { global.cDenB++; local.cDenB++; if(isCrOk) { global.cNumB++; local.cNumB++; } }
                        if (zone.equals("CONSTRUCTION ZONE C")) { global.cDenC++; local.cDenC++; if(isCrOk) { global.cNumC++; local.cNumC++; } }
                    } else if (!rang.isEmpty()) {
                        if (zone.contains("ZONE A")) { global.p2DenA++; local.p2DenA++; if(isCrOk) { global.p2NumA++; local.p2NumA++; } }
                        if (zone.contains("ZONE B")) { global.p2DenB++; local.p2DenB++; if(isCrOk) { global.p2NumB++; local.p2NumB++; } }
                        if (zone.contains("ZONE C")) { global.p2DenC++; local.p2DenC++; if(isCrOk) { global.p2NumC++; local.p2NumC++; } }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur f l'analyse dyal Fichier 2", e);
            throw new RuntimeException("Erreur f l'analyse: " + e.getMessage());
        }

        // Nettoyage dyal DB 9bel l'insertion
        List<String> processToClear = Arrays.asList(
                "TNH",
                "PERF_RANG_1_A", "PERF_RANG_1_B", "PERF_RANG_1_C",
                "HOTLINE_RANG_1_A", "HOTLINE_RANG_1_B", "HOTLINE_RANG_1_C",
                "CONSTRUCTION_RANG_1_A", "CONSTRUCTION_RANG_1_B", "CONSTRUCTION_RANG_1_C",
                "PERF_RANG_2_A", "PERF_RANG_2_B", "PERF_RANG_2_C"
        );
        kpiArchiveRepository.deleteByMoisAndAnneeAndProcessusIn(month, year, processToClear);

        Map<String, Fichier2DeptDataDto> finalDetails = new HashMap<>();
        List<KpiArchive> archivesToSave = new ArrayList<>();

        for (Map.Entry<String, RawStats> entry : statsMap.entrySet()) {
            String dept = entry.getKey();
            RawStats s = entry.getValue();

            long totalDenumRang1 = s.p1DenA + s.p1DenB + s.p1DenC + s.hDenA + s.hDenB + s.hDenC + s.cDenA + s.cDenB + s.cDenC;
            long totalDenumRang2 = s.p2DenA + s.p2DenB + s.p2DenC;

            // TNH
            TnhResultDto tnhDto = new TnhResultDto(s.tnhNum, s.tnhDenum, calc(s.tnhNum, s.tnhDenum));
            archivesToSave.add(buildArchive(month, year, dept, "TNH", s.tnhNum, s.tnhDenum, tnhDto.getResultat(), 0, 0));

            // PERF RANG 1
            PerfGroupDto p1A = buildPerfGroup(s.p1NumA, s.p1DenA, totalDenumRang1, config.getPlp().getA().getMin(), config.getPlp().getA().getMax());
            PerfGroupDto p1B = buildPerfGroup(s.p1NumB, s.p1DenB, totalDenumRang1, config.getPlp().getB().getMin(), config.getPlp().getB().getMax());
            PerfGroupDto p1C = buildPerfGroup(s.p1NumC, s.p1DenC, totalDenumRang1, config.getPlp().getC().getMin(), config.getPlp().getC().getMax());
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_1_A", s.p1NumA, s.p1DenA, p1A.getResultat(), p1A.getPartDeMarche(), p1A.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_1_B", s.p1NumB, s.p1DenB, p1B.getResultat(), p1B.getPartDeMarche(), p1B.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_1_C", s.p1NumC, s.p1DenC, p1C.getResultat(), p1C.getPartDeMarche(), p1C.getBonus()));

            // HOTLINE RANG 1
            HotlineGroupDto h1A = buildHotlineGroup(s.hNumA, s.hDenA, totalDenumRang1, config.getHotline().getA().getMin(), config.getHotline().getA().getMax());
            HotlineGroupDto h1B = buildHotlineGroup(s.hNumB, s.hDenB, totalDenumRang1, config.getHotline().getB().getMin(), config.getHotline().getB().getMax());
            HotlineGroupDto h1C = buildHotlineGroup(s.hNumC, s.hDenC, totalDenumRang1, config.getHotline().getC().getMin(), config.getHotline().getC().getMax());
            archivesToSave.add(buildArchive(month, year, dept, "HOTLINE_RANG_1_A", s.hNumA, s.hDenA, h1A.getResultat(), h1A.getPartDeMarche(), h1A.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "HOTLINE_RANG_1_B", s.hNumB, s.hDenB, h1B.getResultat(), h1B.getPartDeMarche(), h1B.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "HOTLINE_RANG_1_C", s.hNumC, s.hDenC, h1C.getResultat(), h1C.getPartDeMarche(), h1C.getBonus()));

            // CONSTRUCTION RANG 1
            ConstructionGroupDto c1A = buildConstructionGroup(s.cNumA, s.cDenA, totalDenumRang1, config.getConstruction().getA().getMin(), config.getConstruction().getA().getMax());
            ConstructionGroupDto c1B = buildConstructionGroup(s.cNumB, s.cDenB, totalDenumRang1, config.getConstruction().getB().getMin(), config.getConstruction().getB().getMax());
            ConstructionGroupDto c1C = buildConstructionGroup(s.cNumC, s.cDenC, totalDenumRang1, config.getConstruction().getC().getMin(), config.getConstruction().getC().getMax());
            archivesToSave.add(buildArchive(month, year, dept, "CONSTRUCTION_RANG_1_A", s.cNumA, s.cDenA, c1A.getResultat(), c1A.getPartDeMarche(), c1A.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "CONSTRUCTION_RANG_1_B", s.cNumB, s.cDenB, c1B.getResultat(), c1B.getPartDeMarche(), c1B.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "CONSTRUCTION_RANG_1_C", s.cNumC, s.cDenC, c1C.getResultat(), c1C.getPartDeMarche(), c1C.getBonus()));

            // PERF RANG 2
            PerfRang2GroupDto p2A = buildPerfRang2Group(s.p2NumA, s.p2DenA, totalDenumRang2, config.getRang2().getA().getMin(), config.getRang2().getA().getMax());
            PerfRang2GroupDto p2B = buildPerfRang2Group(s.p2NumB, s.p2DenB, totalDenumRang2, config.getRang2().getB().getMin(), config.getRang2().getB().getMax());
            PerfRang2GroupDto p2C = buildPerfRang2Group(s.p2NumC, s.p2DenC, totalDenumRang2, config.getRang2().getC().getMin(), config.getRang2().getC().getMax());
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_2_A", s.p2NumA, s.p2DenA, p2A.getResultat(), p2A.getPartDeMarche(), p2A.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_2_B", s.p2NumB, s.p2DenB, p2B.getResultat(), p2B.getPartDeMarche(), p2B.getBonus()));
            archivesToSave.add(buildArchive(month, year, dept, "PERF_RANG_2_C", s.p2NumC, s.p2DenC, p2C.getResultat(), p2C.getPartDeMarche(), p2C.getBonus()));

            Fichier2DeptDataDto deptDto = Fichier2DeptDataDto.builder()
                    .tnh(tnhDto)
                    .perfRang1(new PerfRang1ResultDto(p1A, p1B, p1C))
                    .hotlineRang1(new HotlineRang1ResultDto(h1A, h1B, h1C))
                    .constructionRang1(new ConstructionRang1ResultDto(c1A, c1B, c1C))
                    .perfRang2(new PerfRang2ResultDto(p2A, p2B, p2C))
                    .build();

            finalDetails.put(dept, deptDto);
        }

        kpiArchiveRepository.saveAll(archivesToSave);
        return Fichier2ResponseDto.builder().details(finalDetails).build();
    }

    private KpiArchive buildArchive(int month, int year, String dept, String process, long num, long denum, double res, double part, double bonus) {
        return KpiArchive.builder()
                .mois(month).annee(year).processus(process).departement(dept)
                .num(num).denum(denum).resultat(res).partDeMarche(part).bonus(bonus).build();
    }

    private BonusConfigDto parseConfigOrDefault(String configJson) {
        if (configJson != null && !configJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(configJson, BonusConfigDto.class);
            } catch (Exception e) { log.error("Error parsing config", e); }
        }
        return BonusConfigDto.builder()
                .plp(new BonusConfigDto.ZoneConfig(new BonusConfigDto.MinMax(93.0, 98.0), new BonusConfigDto.MinMax(90.0, 96.0), new BonusConfigDto.MinMax(86.0, 95.0)))
                .hotline(new BonusConfigDto.ZoneConfig(new BonusConfigDto.MinMax(84.0, 91.0), new BonusConfigDto.MinMax(77.0, 88.0), new BonusConfigDto.MinMax(76.0, 83.0)))
                .construction(new BonusConfigDto.ZoneConfig(new BonusConfigDto.MinMax(78.0, 86.0), new BonusConfigDto.MinMax(74.0, 84.0), new BonusConfigDto.MinMax(68.0, 78.0)))
                .rang2(new BonusConfigDto.ZoneConfig(new BonusConfigDto.MinMax(67.0, 72.0), new BonusConfigDto.MinMax(63.0, 68.0), new BonusConfigDto.MinMax(57.0, 63.0)))
                .build();
    }

    private double calc(long num, long denum) {
        if (denum == 0) return 0.0;
        return Math.round((((double) num / denum) * 100) * 100.0) / 100.0;
    }

    private double calcPart(long localDenum, long globalDenum) {
        if (globalDenum == 0) return 0.0;
        return Math.round((((double) localDenum / globalDenum) * 100) * 100.0) / 100.0;
    }

    private double calcBonus(double resultat, double partDeMarche, double pointMin, double pointMax) {
        double bonusMax = 4.0;
        double partRatio = partDeMarche / 100.0;
        if (resultat <= pointMin) return 0.0;
        else if (resultat >= pointMax) return Math.round((bonusMax * partRatio) * 100.0) / 100.0;
        else {
            double bonus = ((resultat - pointMin) / (pointMax - pointMin)) * bonusMax * partRatio;
            return Math.round(bonus * 100.0) / 100.0;
        }
    }

    private PerfGroupDto buildPerfGroup(long num, long denum, long totalDenumGlobal, double min, double max) {
        double res = calc(num, denum); double part = calcPart(denum, totalDenumGlobal);
        return new PerfGroupDto(num, denum, res, part, calcBonus(res, part, min, max));
    }

    private HotlineGroupDto buildHotlineGroup(long num, long denum, long totalDenumGlobal, double min, double max) {
        double res = calc(num, denum); double part = calcPart(denum, totalDenumGlobal);
        return new HotlineGroupDto(num, denum, res, part, calcBonus(res, part, min, max));
    }

    private ConstructionGroupDto buildConstructionGroup(long num, long denum, long totalDenumGlobal, double min, double max) {
        double res = calc(num, denum); double part = calcPart(denum, totalDenumGlobal);
        return new ConstructionGroupDto(num, denum, res, part, calcBonus(res, part, min, max));
    }

    private PerfRang2GroupDto buildPerfRang2Group(long num, long denum, long totalDenumGlobal, double min, double max) {
        double res = calc(num, denum); double part = calcPart(denum, totalDenumGlobal);
        return new PerfRang2GroupDto(num, denum, res, part, calcBonus(res, part, min, max));
    }
}