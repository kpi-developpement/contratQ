package com.contrat.qualite.controller;

import com.contrat.qualite.entity.KpiArchive;
import com.contrat.qualite.repository.KpiArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class KpiExportController {

    private final KpiArchiveRepository kpiArchiveRepository;

    // HADI HYA L'API LI GHAT-CONSOMIHA F L'APP LOKHRA
    // Exemple: GET http://10.10.10.25:7623/api/v1/export/kpi?month=8&year=2026
    @GetMapping("/kpi")
    public ResponseEntity<List<KpiArchive>> getKpiData(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(value = "processus", required = false) String processus) {

        List<KpiArchive> data;
        if (processus != null && !processus.isEmpty()) {
            data = kpiArchiveRepository.findByMoisAndAnneeAndProcessus(month, year, processus);
        } else {
            data = kpiArchiveRepository.findByMoisAndAnnee(month, year);
        }

        return ResponseEntity.ok(data);
    }
}