package com.contrat.qualite.excelprocessing.savperf.controller;

import com.contrat.qualite.excelprocessing.savperf.dto.SavPerfResultDto;
import com.contrat.qualite.excelprocessing.savperf.service.SavPerfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/savperf")
@RequiredArgsConstructor
public class SavPerfController {

    private final SavPerfService savPerfService;

    @PostMapping("/analyze")
    public ResponseEntity<SavPerfResultDto> analyzeSavPerf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(savPerfService.processSavPerfExcel(file, month, year));
    }
}