package com.contrat.qualite.excelprocessing.sacliok.controller;

import com.contrat.qualite.excelprocessing.sacliok.dto.SacliResultDto;
import com.contrat.qualite.excelprocessing.sacliok.service.SacliService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/sacli")
@RequiredArgsConstructor
public class SacliController {

    private final SacliService sacliService;

    @PostMapping("/analyze")
    public ResponseEntity<SacliResultDto> analyzeSacliExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SacliResultDto result = sacliService.processSacliExcel(file);
        return ResponseEntity.ok(result);
    }
}