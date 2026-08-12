package com.contrat.qualite.repository;

import com.contrat.qualite.entity.KpiArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface KpiArchiveRepository extends JpaRepository<KpiArchive, Long> {

    // Bach n-ms7ou l'9dim ila 3awd injecta nfs ch'her w nfs l'processus
    @Transactional
    void deleteByMoisAndAnneeAndProcessus(int mois, int annee, String processus);
}