"use client";

import styles from "../styles/table.module.css";

export default function MasterTable({ data, activeModule }) {
  if (!data) return null;

  // Function bach n3rfo wach data fiha Multi-Group wla Single
  const isMultiGroup = data.groupA !== undefined;

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

  // Calcul du Total Global ila kanou Multi-groups
  const totalNum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.num, 0) : data.num;
  const totalDenum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.denum, 0) : data.denum;
  const totalResultat = totalDenum > 0 ? ((totalNum / totalDenum) * 100).toFixed(2) : "0.00";

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
              <th className={styles.resultCol}>Taux de Réussite (%)</th>
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
                <td className={styles.resultCol}>
                  <span className={styles.percentageBadge}>
                    {row.data.resultat}%
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
          {isMultiGroup && (
            <tfoot>
              <tr>
                <td className={styles.totalLabel}>TOTAL GLOBAL</td>
                <td className={styles.numCol}>{totalNum.toLocaleString()}</td>
                <td className={styles.denumCol}>{totalDenum.toLocaleString()}</td>
                <td className={styles.resultCol}>
                  <span className={`${styles.percentageBadge} ${styles.totalBadge}`}>
                    {totalResultat}%
                  </span>
                </td>
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </div>
  );
}