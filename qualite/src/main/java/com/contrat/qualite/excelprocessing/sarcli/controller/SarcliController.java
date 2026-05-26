package com.contrat.qualite.excelprocessing.sarcli.controller;

import com.contrat.qualite.excelprocessing.sarcli.dto.SarcliResultDto;
import com.contrat.qualite.excelprocessing.sarcli.service.SarcliService;
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
    public ResponseEntity<SarcliResultDto> analyzeSarcliExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SarcliResultDto result = sarcliService.processSarcliExcel(file);
        return ResponseEntity.ok(result);
    }
}