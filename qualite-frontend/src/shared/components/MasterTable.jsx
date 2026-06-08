"use client";

import styles from "../styles/table.module.css";

export default function MasterTable({ data, activeModule }) {
  if (!data) return null;

  const isMultiGroup = data.groupA !== undefined;
  
  // ZEDNA GAAA3 LES MODULES LI FIHOM L'BONUS HNA
  const isBonusActive = [
    "PERF_RANG_1", "HOTLINE_RANG_1", "CONSTRUCTION_RANG_1", "PERF_RANG_2", 
    "SACLI_OK", "SARCLI_NOK", "GEM_NOK", "TAUX_20J"
  ].includes(activeModule);

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

  const totalNum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.num, 0) : data.num;
  const totalDenum = isMultiGroup ? tableRows.reduce((acc, row) => acc + row.data.denum, 0) : data.denum;
  const totalResultat = totalDenum > 0 ? ((totalNum / totalDenum) * 100).toFixed(2) : "0.00";
  const totalPart = totalDenum > 0 ? "100.00" : "0.00";
  const totalBonus = isBonusActive 
    ? (isMultiGroup ? tableRows.reduce((acc, row) => acc + (row.data.bonus || 0), 0) : (data.bonus || 0)).toFixed(2) 
    : "0.00";

  return (
    <div className={styles.tableWrapper} style={{ marginTop: '30px' }}>
      <div className={styles.tableHeader}>
        <h3 className={styles.tableTitle}>Synthèse Globale - {activeModule?.replace(/_/g, ' ')}</h3>
        <span className={styles.tableBadge}>DATA VERIFIED</span>
      </div>

      <div className={styles.tableContainer}>
        <table className={styles.premiumTable} style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center' }}>
          <thead>
            <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0' }}>
              <th style={{ padding: '15px', textAlign: 'left', color: '#475569' }}>Indicateur / Groupe</th>
              <th style={{ padding: '15px', color: '#475569' }}>NUM</th>
              <th style={{ padding: '15px', color: '#475569' }}>DENUM</th>
              {isMultiGroup && <th style={{ padding: '15px', color: '#8b5cf6' }}>Part de Marché</th>}
              <th style={{ padding: '15px', color: '#10b981' }}>Taux de Réussite</th>
              {isBonusActive && <th style={{ padding: '15px', color: '#F59E0B' }}>Bonus Gagné</th>}
            </tr>
          </thead>
          <tbody>
            {tableRows.map((row, index) => (
              <tr key={row.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.2s', animationDelay: `${index * 0.1}s` }}>
                <td style={{ padding: '15px', textAlign: 'left', fontWeight: 'bold', color: '#1e293b' }}>
                  <div style={{ display: 'inline-block', width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#3b82f6', marginRight: '10px' }}></div>
                  {row.label}
                </td>
                <td style={{ padding: '15px', color: '#64748b', fontWeight: '500' }}>{row.data.num.toLocaleString()}</td>
                <td style={{ padding: '15px', color: '#64748b', fontWeight: '500' }}>{row.data.denum.toLocaleString()}</td>
                
                {isMultiGroup && (
                  <td style={{ padding: '15px' }}>
                    <span style={{ color: '#8b5cf6', fontWeight: 'bold', backgroundColor: 'rgba(139, 92, 246, 0.1)', padding: '6px 12px', borderRadius: '8px', display: 'inline-block' }}>
                      {row.data.partDeMarche || 0}%
                    </span>
                  </td>
                )}

                <td style={{ padding: '15px' }}>
                  <span style={{ color: '#10b981', fontWeight: 'bold', backgroundColor: 'rgba(16, 185, 129, 0.1)', padding: '6px 12px', borderRadius: '8px', display: 'inline-block' }}>
                    {row.data.resultat}%
                  </span>
                </td>

                {isBonusActive && (
                  <td style={{ padding: '15px' }}>
                    <span style={{ color: '#F59E0B', fontWeight: '900', backgroundColor: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)', padding: '6px 12px', borderRadius: '8px', display: 'inline-block', boxShadow: '0 0 10px rgba(245, 158, 11, 0.2)' }}>
                      + {row.data.bonus || 0}%
                    </span>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr style={{ backgroundColor: '#f8fafc', borderTop: '2px solid #e2e8f0', fontWeight: 'bold' }}>
              <td style={{ padding: '18px', textAlign: 'left', color: '#0f172a' }}>TOTAL GLOBAL</td>
              <td style={{ padding: '18px', color: '#0f172a' }}>{totalNum.toLocaleString()}</td>
              <td style={{ padding: '18px', color: '#0f172a' }}>{totalDenum.toLocaleString()}</td>
              {isMultiGroup && (
                <td style={{ padding: '18px' }}>
                  <span style={{ color: '#8b5cf6', fontWeight: '900' }}>{totalPart}%</span>
                </td>
              )}
              <td style={{ padding: '18px' }}>
                <span style={{ color: '#fff', backgroundColor: '#10b981', padding: '8px 16px', borderRadius: '8px', boxShadow: '0 4px 10px rgba(16, 185, 129, 0.3)' }}>
                  {totalResultat}%
                </span>
              </td>
              {isBonusActive && (
                <td style={{ padding: '18px' }}>
                  <span className="gold-pulse-bg" style={{ color: '#fff', background: 'linear-gradient(135deg, #F59E0B, #D97706)', padding: '8px 16px', borderRadius: '8px', fontWeight: '900' }}>
                    + {totalBonus}%
                  </span>
                </td>
              )}
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}