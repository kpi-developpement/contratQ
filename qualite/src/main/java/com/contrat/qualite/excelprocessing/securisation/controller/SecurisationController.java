package com.contrat.qualite.excelprocessing.securisation.controller;

import com.contrat.qualite.excelprocessing.securisation.dto.SecurisationResultDto;
import com.contrat.qualite.excelprocessing.securisation.service.SecurisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/excel/securisation")
@RequiredArgsConstructor
public class SecurisationController {

    private final SecurisationService securisationService;

    @PostMapping("/analyze")
    public ResponseEntity<SecurisationResultDto> analyzeSecurisation(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(securisationService.processSecurisationExcel(file));
    }
}