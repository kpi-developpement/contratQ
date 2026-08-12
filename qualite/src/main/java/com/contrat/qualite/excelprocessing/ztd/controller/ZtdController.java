package com.contrat.qualite.excelprocessing.ztd.controller;

import com.contrat.qualite.excelprocessing.ztd.dto.ZtdResultDto;
import com.contrat.qualite.excelprocessing.ztd.service.ZtdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/ztd")
@RequiredArgsConstructor
public class ZtdController {

    private final ZtdService ztdService;

    @PostMapping("/analyze")
    public ResponseEntity<ZtdResultDto> analyzeZtd(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ztdService.processZtdExcel(file, month, year));
    }
}