"use client";

import styles from "../styles/table.module.css";

export default function MasterTable({ data, activeModule }) {
  if (!data) return null;

  const isMultiGroup = data.groupA !== undefined;
  
  // Nchoufo wach had l'module fih l'bonus (Ghir RANG 1)
  const isBonusActive = ["PERF_RANG_1", "HOTLINE_RANG_1", "CONSTRUCTION_RANG_1"].includes(activeModule);

  let tableRows = [];

  if (isMultiGroup) {
    const getZoneLabel = (letter) => {
      if (activeModule === "PERF_RANG_1") return `PLP ZONE ${letter}`;
      if (activeModule === "HOTLINE_RANG_1") return `HOTLINE ZONE ${letter}`;
      if (activeModule === "CONSTRUCTION_RANG_1") return `CONSTRUCT ZONE ${letter}`;
      if (activeModule === "PERF_RANG_2") return `GLOBAL ZONE ${letter}`;
      return `ZONE ${letter}`;
    };

    tableRows = [
      { id: 'A', label: `Groupe A (${getZoneLabel('A')})`, data: data.groupA },
      { id: 'B', label: `Groupe B (${getZoneLabel('B')})`, data: data.groupB },
      { id: 'C', label: `Groupe C (${getZoneLabel('C')})`, data: data.groupC },
    ];
  } else {
    tableRows = [
      { id: 'GLOBAL', label: 'Traitement Global', data: data },
    ];
  }

  // Les Totaux
  const totalNum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.num, 0) : data.num;
  const totalDenum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.denum, 0) : data.denum;
  const totalResultat = totalDenum > 0 ? ((totalNum / totalDenum) * 100).toFixed(2) : "0.00";
  const totalPart = totalDenum > 0 ? "100.00" : "0.00";
  
  // Total Bonus (Bima ana l'Backend kiderbou f Part de marché, jm3hom kaye3ti l'Total S7i7!)
  const totalBonus = isBonusActive ? tableRows.reduce((acc, row) => acc + (row.data.bonus || 0), 0).toFixed(2) : "0.00";

  return (
    <div className={styles.tableWrapper}>
      <div className={styles.tableHeader}>
        <h3 className={styles.tableTitle}>Synthèse Globale</h3>
        <span className={styles.tableBadge}>DATA VERIFIED</span>
      </div>

      <div className={styles.tableContainer}>
        <table className={styles.premiumTable}>
          <thead>
            <tr>
              <th>Indicateur / Groupe</th>
              <th className={styles.numCol}>Numérateur (NUM)</th>
              <th className={styles.denumCol}>Dénominateur (DENUM)</th>
              {isMultiGroup && <th className={styles.denumCol}>Part de Marché</th>}
              <th className={styles.resultCol}>Taux de Réussite</th>
              {isBonusActive && <th className={styles.resultCol}>Bonus Gagné (Max 4%)</th>}
            </tr>
          </thead>
          <tbody>
            {tableRows.map((row, index) => (
              <tr key={row.id} style={{ animationDelay: `${index * 0.1}s` }}>
                <td className={styles.rowLabel}>
                  <div className={styles.labelIndicator}></div>
                  {row.label}
                </td>
                <td className={styles.numCol}>{row.data.num.toLocaleString()}</td>
                <td className={styles.denumCol}>{row.data.denum.toLocaleString()}</td>
                
                {isMultiGroup && (
                  <td className={styles.denumCol}>
                    <span style={{color: '#8b5cf6', fontWeight: 'bold', background: 'rgba(139, 92, 246, 0.1)', padding: '6px 12px', borderRadius: '10px'}}>
                      {row.data.partDeMarche || 0}%
                    </span>
                  </td>
                )}

                <td className={styles.resultCol}>
                  <span className={styles.percentageBadge}>
                    {row.data.resultat}%
                  </span>
                </td>

                {isBonusActive && (
                  <td className={styles.resultCol}>
                    <span style={{color: '#F59E0B', fontWeight: '900', background: 'rgba(245, 158, 11, 0.1)', padding: '6px 12px', borderRadius: '10px', border: '1px solid rgba(245, 158, 11, 0.3)'}}>
                      + {row.data.bonus || 0}%
                    </span>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
          {isMultiGroup && (
            <tfoot>
              <tr>
                <td className={styles.totalLabel}>TOTAL GLOBAL</td>
                <td className={styles.numCol}>{totalNum.toLocaleString()}</td>
                <td className={styles.denumCol}>{totalDenum.toLocaleString()}</td>
                <td className={styles.denumCol}>
                  <span style={{color: '#8b5cf6', fontWeight: '900'}}>{totalPart}%</span>
                </td>
                <td className={styles.resultCol}>
                  <span className={`${styles.percentageBadge} ${styles.totalBadge}`}>
                    {totalResultat}%
                  </span>
                </td>
                {isBonusActive && (
                  <td className={styles.resultCol}>
                    <span style={{color: '#fff', background: 'linear-gradient(135deg, #F59E0B, #D97706)', padding: '8px 16px', borderRadius: '12px', fontWeight: '900', boxShadow: '0 4px 15px rgba(245, 158, 11, 0.4)'}}>
                      + {totalBonus}%
                    </span>
                  </td>
                )}
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </div>
  );
}