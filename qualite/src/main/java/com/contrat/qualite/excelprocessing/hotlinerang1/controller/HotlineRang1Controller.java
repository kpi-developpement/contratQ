package com.contrat.qualite.excelprocessing.hotlinerang1.controller;

import com.contrat.qualite.excelprocessing.hotlinerang1.dto.HotlineRang1ResultDto;
import com.contrat.qualite.excelprocessing.hotlinerang1.service.HotlineRang1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/hotlinerang1")
@RequiredArgsConstructor
public class HotlineRang1Controller {

    private final HotlineRang1Service hotlineRang1Service;

    @PostMapping("/analyze")
    public ResponseEntity<HotlineRang1ResultDto> analyzeHotlineRang1Excel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        HotlineRang1ResultDto result = hotlineRang1Service.processHotlineRang1Excel(file);
        return ResponseEntity.ok(result);
    }
}