package com.contrat.qualite.excelprocessing.gemnok.controller;

import com.contrat.qualite.excelprocessing.gemnok.dto.GemNokResultDto;
import com.contrat.qualite.excelprocessing.gemnok.service.GemNokService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/gemnok")
@RequiredArgsConstructor
public class GemNokController {

    private final GemNokService gemNokService;

    @PostMapping("/analyze")
    public ResponseEntity<GemNokResultDto> analyzeGemNok(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "config", required = false) String configJson,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(gemNokService.processGemNokExcel(file, configJson, month, year));
    }
}