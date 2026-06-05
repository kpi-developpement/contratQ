package com.contrat.qualite.excelprocessing.dashboard.controller;

import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier2ResponseDto;
import com.contrat.qualite.excelprocessing.dashboard.dto.Fichier3ResponseDto;
import com.contrat.qualite.excelprocessing.dashboard.service.Fichier2AggregatorService;
import com.contrat.qualite.excelprocessing.dashboard.service.Fichier3AggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final Fichier2AggregatorService fichier2Service;
    private final Fichier3AggregatorService fichier3Service;

    @PostMapping("/fichier2")
    public ResponseEntity<Fichier2ResponseDto> processFichier2(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "config", required = false) String configJson) {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(fichier2Service.processFichier2(file, configJson));
    }

    @PostMapping("/fichier3")
    public ResponseEntity<Fichier3ResponseDto> processFichier3(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(fichier3Service.processFichier3(file));
    }
}