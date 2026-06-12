package com.contrat.qualite.excelprocessing.sav.controller;

import com.contrat.qualite.excelprocessing.sav.dto.SavResultDto;
import com.contrat.qualite.excelprocessing.sav.service.SavService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/sav")
@RequiredArgsConstructor
public class SavController {

    private final SavService savService;

    @PostMapping("/perf")
    public ResponseEntity<SavResultDto> analyzeSavPerf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(savService.processSavPerf(file));
    }

    @PostMapping("/delai")
    public ResponseEntity<SavResultDto> analyzeSavDelai(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(savService.processSavDelai(file));
    }
}