package com.contrat.qualite.excelprocessing.sarclinok.controller;

import com.contrat.qualite.excelprocessing.sarclinok.dto.SarcliResultDto;
import com.contrat.qualite.excelprocessing.sarclinok.service.SarcliService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/sarcli")
@RequiredArgsConstructor
public class SarcliController {

    private final SarcliService sarcliService;

    @PostMapping("/analyze")
    public ResponseEntity<SarcliResultDto> analyzeSarcliExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "config", required = false) String configJson,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(sarcliService.processSarcliExcel(file, configJson, month, year));
    }
}