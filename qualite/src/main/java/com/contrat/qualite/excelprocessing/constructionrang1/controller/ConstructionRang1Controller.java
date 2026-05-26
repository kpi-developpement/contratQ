package com.contrat.qualite.excelprocessing.constructionrang1.controller;

import com.contrat.qualite.excelprocessing.constructionrang1.dto.ConstructionRang1ResultDto;
import com.contrat.qualite.excelprocessing.constructionrang1.service.ConstructionRang1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/constructionrang1")
@RequiredArgsConstructor
public class ConstructionRang1Controller {

    private final ConstructionRang1Service constructionRang1Service;

    @PostMapping("/analyze")
    public ResponseEntity<ConstructionRang1ResultDto> analyzeConstructionRang1Excel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ConstructionRang1ResultDto result = constructionRang1Service.processConstructionRang1Excel(file);
        return ResponseEntity.ok(result);
    }
}