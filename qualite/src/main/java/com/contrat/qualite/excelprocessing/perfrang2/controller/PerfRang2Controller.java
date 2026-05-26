package com.contrat.qualite.excelprocessing.perfrang2.controller;

import com.contrat.qualite.excelprocessing.perfrang2.dto.PerfRang2ResultDto;
import com.contrat.qualite.excelprocessing.perfrang2.service.PerfRang2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/perfrang2")
@RequiredArgsConstructor
public class PerfRang2Controller {

    private final PerfRang2Service perfRang2Service;

    @PostMapping("/analyze")
    public ResponseEntity<PerfRang2ResultDto> analyzePerfRang2Excel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PerfRang2ResultDto result = perfRang2Service.processPerfRang2Excel(file);
        return ResponseEntity.ok(result);
    }
}