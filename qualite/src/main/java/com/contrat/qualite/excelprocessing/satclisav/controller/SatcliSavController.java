package com.contrat.qualite.excelprocessing.satclisav.controller;

import com.contrat.qualite.excelprocessing.satclisav.dto.SatcliSavResultDto;
import com.contrat.qualite.excelprocessing.satclisav.service.SatcliSavService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/satclisav")
@RequiredArgsConstructor
public class SatcliSavController {

    private final SatcliSavService satcliSavService;

    @PostMapping("/analyze")
    public ResponseEntity<SatcliSavResultDto> analyzeSatcliSav(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(satcliSavService.processSatcliSavExcel(file));
    }
}