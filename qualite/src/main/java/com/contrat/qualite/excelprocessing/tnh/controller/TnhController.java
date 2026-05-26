package com.contrat.qualite.excelprocessing.tnh.controller;

import com.contrat.qualite.excelprocessing.tnh.dto.TnhResultDto;
import com.contrat.qualite.excelprocessing.tnh.service.TnhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/tnh")
@RequiredArgsConstructor
public class TnhController {

    private final TnhService tnhService;

    @PostMapping("/analyze")
    public ResponseEntity<TnhResultDto> analyzeTnhExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        TnhResultDto result = tnhService.processTnhExcel(file);
        return ResponseEntity.ok(result);
    }
}