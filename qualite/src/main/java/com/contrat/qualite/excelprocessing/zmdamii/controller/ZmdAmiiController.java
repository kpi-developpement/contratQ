package com.contrat.qualite.excelprocessing.zmdamii.controller;

import com.contrat.qualite.excelprocessing.zmdamii.dto.ZmdAmiiResultDto;
import com.contrat.qualite.excelprocessing.zmdamii.service.ZmdAmiiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/zmdamii")
@RequiredArgsConstructor
public class ZmdAmiiController {

    private final ZmdAmiiService zmdAmiiService;

    @PostMapping("/analyze")
    public ResponseEntity<ZmdAmiiResultDto> analyzeZmdAmii(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "year") int year) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(zmdAmiiService.processZmdAmiiExcel(file, month, year));
    }
}