"use client";

import React from "react";

export default function GlobalViewModal({ onClose, globalRows }) {
  
  // L'FIX HWA HNA: Calcul dyal l'Total RACC Bonus
  const raccRows = globalRows.filter(r => r.category === 'RACC');
  const totalRaccBonus = raccRows.reduce((sum, row) => sum + (row.data?.bonus || 0), 0);
  const finalScore = 90 + totalRaccBonus;

  return (
    <>
      <style dangerouslySetInnerHTML={{__html: `
        .globalModalOverlay {
          position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
          background: rgba(8, 12, 26, 0.85); backdrop-filter: blur(12px);
          z-index: 9999; display: flex; justify-content: center; align-items: center;
          animation: cyberFadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }
        .globalModalContent {
          background: #0f1424; width: 92%; max-width: 1250px; max-height: 88vh;
          border-radius: 16px; padding: 35px; overflow-y: auto;
          border: 1px solid rgba(59, 130, 246, 0.2);
          box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6), 0 0 40px rgba(59, 130, 246, 0.05);
          position: relative;
          animation: cyberSlideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }
        .globalCloseBtn {
          position: absolute; top: 25px; right: 25px; background: rgba(30, 41, 59, 0.5); border: 1px solid rgba(255,255,255,0.05);
          width: 38px; height: 48px; border-radius: 50%; cursor: pointer;
          display: flex; justify-content: center; align-items: center; color: #94a3b8; transition: all 0.25s ease;
        }
        .globalCloseBtn:hover { background: rgba(239, 68, 68, 0.15); border-color: rgba(239, 68, 68, 0.4); color: #ef4444; transform: rotate(90deg); }
        
        .globalTable { width: 100%; border-collapse: separate; border-spacing: 0; margin-bottom: 35px; text-align: left; }
        .globalTable th { background: #171e36; padding: 16px 20px; color: #94a3b8; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; border-bottom: 2px solid rgba(59, 130, 246, 0.2); }
        .globalTable td { padding: 16px 20px; border-bottom: 1px solid rgba(255, 255, 255, 0.03); color: #e2e8f0; font-weight: 500; font-size: 14px; transition: all 0.2s ease; }
        .globalTable tr:hover td { background: rgba(59, 130, 246, 0.04); color: #fff; }
        
        .cyberBadge { display: inline-block; padding: 6px 12px; border-radius: 6px; font-weight: 700; font-size: 12px; letter-spacing: 0.5px; }
        .badgeSuccess { background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #10b981; box-shadow: 0 0 10px rgba(16, 185, 129, 0.05); }
        .badgeBonus { background: rgba(245, 158, 11, 0.08); border: 1px solid rgba(245, 158, 11, 0.2); color: #f59e0b; font-weight: 800; text-shadow: 0 0 8px rgba(245, 158, 11, 0.2); }
        
        .modalSubTitle { font-size: 15px; font-weight: 700; color: #3b82f6; text-transform: uppercase; letter-spacing: 1.5px; margin: 30px 0 15px 0; display: flex; align-items: center; gap: 10px; }
        
        @keyframes cyberFadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes cyberSlideUp { from { opacity: 0; transform: translateY(30px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }
        
        .globalModalContent::-webkit-scrollbar { width: 6px; }
        .globalModalContent::-webkit-scrollbar-track { background: rgba(0,0,0,0.1); }
        .globalModalContent::-webkit-scrollbar-thumb { background: rgba(59, 130, 246, 0.2); border-radius: 10px; }
        .globalModalContent::-webkit-scrollbar-thumb:hover { background: rgba(59, 130, 246, 0.4); }
      `}} />

      <div className="globalModalOverlay" onClick={onClose}>
        <div className="globalModalContent" onClick={(e) => e.stopPropagation()}>
          <button className="globalCloseBtn" onClick={onClose}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.5"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
          
          <h2 style={{ margin: '0 0 25px 0', color: '#fff', display: 'flex', alignItems: 'center', gap: '12px', fontSize: '22px', fontWeight: '800', letterSpacing: '-0.5px' }}>
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#3b82f6" strokeWidth="2.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
            Synthèse Opérationnelle Directe
          </h2>
          
          <div className="modalSubTitle">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.5"><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>
            Flux Qualité RACC
          </div>
          <table className="globalTable">
            <thead>
              <tr>
                <th style={{ borderTopLeftRadius: '8px' }}>Indicateur Métrique</th><th>NUM</th><th>DENUM</th><th>Performance Brut</th><th style={{ borderTopRightRadius: '8px' }}>Bonus Consolidé</th>
              </tr>
            </thead>
            <tbody>
              {raccRows.map(row => (
                <tr key={row.id}>
                  <td style={{ fontWeight: '700', color: '#fff' }}>{row.label}</td>
                  <td style={{ color: '#94a3b8' }}>{row.data ? row.data.num?.toLocaleString() : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td style={{ color: '#94a3b8' }}>{row.data ? row.data.denum?.toLocaleString() : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td>{row.data ? <span className="cyberBadge badgeSuccess">{row.data.resultat}%</span> : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td>{row.data?.bonus !== undefined ? <span className="cyberBadge badgeBonus">+ {row.data.bonus}%</span> : <span style={{ color: '#334155' }}>—</span>}</td>
                </tr>
              ))}
            </tbody>
            {/* L'FIX HWA HNA: Ligne VIP dyal l'Total RACC */}
            <tfoot>
              <tr style={{ backgroundColor: 'rgba(16, 185, 129, 0.05)' }}>
                <td colSpan="4" style={{ textAlign: 'right', fontWeight: '800', color: '#fff', fontSize: '16px', borderBottomLeftRadius: '8px' }}>
                  SCORE DE BASE (90%) + TOTAL BONUS RACC (+{totalRaccBonus.toFixed(2)}%) =
                </td>
                <td style={{ fontWeight: '900', fontSize: '18px', borderBottomRightRadius: '8px' }}>
                  <span className="cyberBadge" style={{ background: 'linear-gradient(135deg, #10b981, #059669)', color: '#fff', boxShadow: '0 0 15px rgba(16, 185, 129, 0.4)', border: 'none' }}>
                    {finalScore.toFixed(2)}%
                  </span>
                </td>
              </tr>
            </tfoot>
          </table>

          <div className="modalSubTitle" style={{ color: '#8b5cf6' }}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg>
            Flux Qualité SAV
          </div>
          <table className="globalTable">
            <thead>
              <tr>
                <th style={{ borderTopLeftRadius: '8px' }}>Indicateur Métrique</th><th>NUM</th><th>DENUM</th><th>Performance Brut</th><th style={{ borderTopRightRadius: '8px' }}>Bonus Consolidé</th>
              </tr>
            </thead>
            <tbody>
              {globalRows.filter(r => r.category === 'SAV').map(row => (
                <tr key={row.id}>
                  <td style={{ fontWeight: '700', color: '#fff' }}>{row.label}</td>
                  <td style={{ color: '#94a3b8' }}>{row.data ? row.data.num?.toLocaleString() : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td style={{ color: '#94a3b8' }}>{row.data ? row.data.denum?.toLocaleString() : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td>{row.data ? <span className="cyberBadge badgeSuccess">{row.data.resultat}%</span> : <span style={{ color: '#334155' }}>—</span>}</td>
                  <td>{row.data?.bonus !== undefined ? <span className="cyberBadge badgeBonus">+ {row.data.bonus}%</span> : <span style={{ color: '#334155' }}>—</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}