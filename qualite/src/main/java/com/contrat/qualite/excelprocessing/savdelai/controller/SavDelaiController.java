package com.contrat.qualite.excelprocessing.savdelai.controller;

import com.contrat.qualite.excelprocessing.savdelai.dto.SavDelaiResultDto;
import com.contrat.qualite.excelprocessing.savdelai.service.SavDelaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/savdelai")
@RequiredArgsConstructor
public class SavDelaiController {

    private final SavDelaiService savDelaiService;

    @PostMapping("/analyze")
    public ResponseEntity<SavDelaiResultDto> analyzeSavDelai(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(savDelaiService.processSavDelaiExcel(file));
    }
}