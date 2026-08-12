package com.contrat.qualite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_archive")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mois", nullable = false)
    private int mois;

    @Column(name = "annee", nullable = false)
    private int annee;

    @Column(name = "processus", nullable = false)
    private String processus; // Ex: SACLI_OK, ZMD_AMII...

    @Column(name = "departement", nullable = false)
    private String departement; // Ex: "GLOBAL", "22", "33"

    @Column(name = "num")
    private long num;

    @Column(name = "denum")
    private long denum;

    @Column(name = "resultat")
    private double resultat;

    @Column(name = "part_de_marche")
    private double partDeMarche;

    @Column(name = "bonus")
    private double bonus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}