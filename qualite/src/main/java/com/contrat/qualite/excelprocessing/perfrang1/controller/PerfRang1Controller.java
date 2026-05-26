package com.contrat.qualite.excelprocessing.perfrang1.controller;

import com.contrat.qualite.excelprocessing.perfrang1.dto.PerfRang1ResultDto;
import com.contrat.qualite.excelprocessing.perfrang1.service.PerfRang1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/perfrang1")
@RequiredArgsConstructor
public class PerfRang1Controller {

    private final PerfRang1Service perfRang1Service;

    @PostMapping("/analyze")
    public ResponseEntity<PerfRang1ResultDto> analyzePerfRang1Excel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PerfRang1ResultDto result = perfRang1Service.processPerfRang1Excel(file);
        return ResponseEntity.ok(result);
    }
}