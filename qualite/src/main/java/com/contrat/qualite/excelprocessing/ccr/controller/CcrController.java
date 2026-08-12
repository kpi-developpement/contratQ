package com.contrat.qualite.excelprocessing.ccr.controller;

import com.contrat.qualite.excelprocessing.ccr.dto.CcrResultDto;
import com.contrat.qualite.excelprocessing.ccr.service.CcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/ccr")
@RequiredArgsConstructor
public class CcrController {

    private final CcrService ccrService;

    @PostMapping("/analyze")
    public ResponseEntity<CcrResultDto> analyzeCcr(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ccrService.processCcrExcel(file, month, year));
    }
}