package com.contrat.qualite.excelprocessing.zmdrip.controller;

import com.contrat.qualite.excelprocessing.zmdrip.dto.ZmdRipResultDto;
import com.contrat.qualite.excelprocessing.zmdrip.service.ZmdRipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/zmdrip")
@RequiredArgsConstructor
public class ZmdRipController {

    private final ZmdRipService zmdRipService;

    @PostMapping("/analyze")
    public ResponseEntity<ZmdRipResultDto> analyzeZmdRip(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(zmdRipService.processZmdRipExcel(file, month, year));
    }
}