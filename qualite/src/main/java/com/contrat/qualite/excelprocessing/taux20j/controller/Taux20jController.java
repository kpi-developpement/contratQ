package com.contrat.qualite.excelprocessing.taux20j.controller;

import com.contrat.qualite.excelprocessing.taux20j.dto.Taux20jResultDto;
import com.contrat.qualite.excelprocessing.taux20j.service.Taux20jService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/taux20j")
@RequiredArgsConstructor
public class Taux20jController {

    private final Taux20jService taux20jService;

    @PostMapping("/analyze")
    public ResponseEntity<Taux20jResultDto> analyzeTaux20j(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "config", required = false) String configJson) {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(taux20jService.processTaux20jExcel(file, configJson));
    }
}