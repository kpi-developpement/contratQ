package com.contrat.qualite.excelprocessing.dashboard.controller;

import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier1ResponseDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier2ResponseDto;
import com.contrat.qualite.excelprocessing.dashboard.service.Fichier1AggregatorService;
import com.contrat.qualite.excelprocessing.dashboard.service.Fichier2AggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    // L'injection dyal les services jdad b Lombok (@RequiredArgsConstructor)
    private final Fichier1AggregatorService fichier1Service;
    private final Fichier2AggregatorService fichier2Service;

    // Endpoint dyal Fichier 1 (SACLI OK, SARCLI NOK)
    @PostMapping("/fichier1")
    public ResponseEntity<Fichier1ResponseDto> processFichier1(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Fichier1ResponseDto result = fichier1Service.processFichier1(file);
        return ResponseEntity.ok(result);
    }

    // Endpoint dyal Fichier 2 (TNH, PERF RANG 1, HOTLINE RANG 1, CONSTRUCTION RANG 1, PERF RANG 2)
    @PostMapping("/fichier2")
    public ResponseEntity<Fichier2ResponseDto> processFichier2(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Fichier2ResponseDto result = fichier2Service.processFichier2(file);
        return ResponseEntity.ok(result);
    }
}