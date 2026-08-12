package com.contrat.qualite.repository;

import com.contrat.qualite.entity.KpiArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface KpiArchiveRepository extends JpaRepository<KpiArchive, Long> {

    @Transactional
    void deleteByMoisAndAnneeAndProcessus(int mois, int annee, String processus);

    @Transactional
    void deleteByMoisAndAnneeAndProcessusIn(int mois, int annee, List<String> processusList);

    List<KpiArchive> findByMoisAndAnnee(int mois, int annee);
    List<KpiArchive> findByMoisAndAnneeAndProcessus(int mois, int annee, String processus);
}