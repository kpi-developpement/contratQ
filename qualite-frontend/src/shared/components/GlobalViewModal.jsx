"use client";

import React from "react";

export default function GlobalViewModal({ onClose, globalRows }) {
  return (
    <>
      <style dangerouslySetInnerHTML={{__html: `
        .globalModalOverlay {
          position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
          background: rgba(15, 23, 42, 0.8); backdrop-filter: blur(8px);
          z-index: 9999; display: flex; justify-content: center; align-items: center;
          animation: fadeIn 0.3s ease;
        }
        .globalModalContent {
          background: white; width: 90%; max-width: 1200px; max-height: 90vh;
          border-radius: 20px; padding: 30px; overflow-y: auto;
          box-shadow: 0 25px 50px rgba(0,0,0,0.25); position: relative;
          animation: slideUp 0.4s ease;
        }
        .globalCloseBtn {
          position: absolute; top: 20px; right: 20px; background: #f1f5f9; border: none;
          width: 40px; height: 40px; border-radius: 50%; cursor: pointer;
          display: flex; justify-content: center; align-items: center; color: #64748b; transition: 0.2s;
        }
        .globalCloseBtn:hover { background: #e2e8f0; color: #ef4444; }
        
        .globalTable { width: 100%; border-collapse: collapse; margin-bottom: 30px; text-align: left; }
        .globalTable th { background: #f8fafc; padding: 15px; color: #475569; border-bottom: 2px solid #cbd5e1; }
        .globalTable td { padding: 15px; border-bottom: 1px solid #f1f5f9; color: #334155; font-weight: 500; }
        .globalTable tr:hover td { background: #f8fafc; }
        .dataBadge { display: inline-block; padding: 5px 10px; border-radius: 6px; font-weight: bold; font-size: 13px; }
        
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes slideUp { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }
      `}} />

      <div className="globalModalOverlay" onClick={onClose}>
        <div className="globalModalContent" onClick={(e) => e.stopPropagation()}>
          <button className="globalCloseBtn" onClick={onClose}>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
          
          <h2 style={{ margin: '0 0 20px 0', color: '#0f172a', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#3b82f6" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
            Synthèse Globale KyntusOS
          </h2>
          
          <h3 style={{ color: '#3b82f6', borderBottom: '2px solid #eff6ff', paddingBottom: '10px' }}>📊 Catégorie RACC</h3>
          <table className="globalTable">
            <thead>
              <tr>
                <th>Indicateur</th><th>NUM</th><th>DENUM</th><th>Taux de Réussite</th><th>Bonus (Gagné)</th>
              </tr>
            </thead>
            <tbody>
              {globalRows.filter(r => r.category === 'RACC').map(row => (
                <tr key={row.id}>
                  <td style={{ fontWeight: 'bold' }}>{row.label}</td>
                  <td>{row.data ? row.data.num?.toLocaleString() : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data ? row.data.denum?.toLocaleString() : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data ? <span className="dataBadge" style={{ background: '#ecfdf5', color: '#10b981' }}>{row.data.resultat}%</span> : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data?.bonus !== undefined ? <span className="dataBadge" style={{ background: '#fef3c7', color: '#F59E0B' }}>+ {row.data.bonus}%</span> : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <h3 style={{ color: '#8b5cf6', borderBottom: '2px solid #f5f3ff', paddingBottom: '10px' }}>🛠️ Catégorie SAV</h3>
          <table className="globalTable">
            <thead>
              <tr>
                <th>Indicateur</th><th>NUM</th><th>DENUM</th><th>Taux de Réussite</th><th>Bonus (Gagné)</th>
              </tr>
            </thead>
            <tbody>
              {globalRows.filter(r => r.category === 'SAV').map(row => (
                <tr key={row.id}>
                  <td style={{ fontWeight: 'bold' }}>{row.label}</td>
                  <td>{row.data ? row.data.num?.toLocaleString() : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data ? row.data.denum?.toLocaleString() : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data ? <span className="dataBadge" style={{ background: '#ecfdf5', color: '#10b981' }}>{row.data.resultat}%</span> : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                  <td>{row.data?.bonus !== undefined ? <span className="dataBadge" style={{ background: '#fef3c7', color: '#F59E0B' }}>+ {row.data.bonus}%</span> : <span style={{ color: '#cbd5e1' }}>-</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}