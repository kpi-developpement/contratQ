"use client";

import React, { useState, useRef, useEffect } from "react";
import InteractiveBackground from "../threejs/InteractiveBackground";
import ShatteredGlass from "./ShatteredGlass";
import ResultCard from "./ResultCard";
import MasterTable from "./MasterTable";
import GlobalViewModal from "./GlobalViewModal";
import styles from "../styles/unified.module.css";

// 1. ZEDNA LES ENDPOINTS ISOLÉS DYAL ZMD W ZTD
const API_ENDPOINTS_ISOLATED = {
  // RACC
  SACLI_OK: "/api/v1/excel/sacli/analyze",
  SARCLI_NOK: "/api/v1/excel/sarcli/analyze",
  GEM_NOK: "/api/v1/excel/gemnok/analyze",
  TAUX_20J: "/api/v1/excel/taux20j/analyze",
  ZMD_AMII: "/api/v1/excel/zmdamii/analyze",
  ZMD_RIP: "/api/v1/excel/zmdrip/analyze",
  ZTD: "/api/v1/excel/ztd/analyze",
  
  // SAV
  SAV_PERF: "/api/v1/excel/savperf/analyze",
  SAV_DELAI: "/api/v1/excel/savdelai/analyze",
  SECURISATION: "/api/v1/excel/securisation/analyze",
  CCR: "/api/v1/excel/ccr/analyze",
  SATCLI_SAV: "/api/v1/excel/satclisav/analyze",
};

const INITIAL_CONFIG = {
  plp: { a: { min: 93.0, max: 98.0 }, b: { min: 90.0, max: 96.0 }, c: { min: 86.0, max: 95.0 } },
  hotline: { a: { min: 84.0, max: 91.0 }, b: { min: 77.0, max: 88.0 }, c: { min: 76.0, max: 83.0 } },
  construction: { a: { min: 78.0, max: 86.0 }, b: { min: 74.0, max: 84.0 }, c: { min: 68.0, max: 78.0 } },
  rang2: { a: { min: 67.0, max: 72.0 }, b: { min: 63.0, max: 68.0 }, c: { min: 57.0, max: 63.0 } },
  sacli: { min: 80.0, max: 95.0, bonusMax: 2.0 },
  sarcli: { min: 30.0, max: 55.0, bonusMax: 1.0 },
  gemNok: { min: 5.0, max: 2.0, bonusMax: 2.0 },
  taux20j: { min: 80.0, max: 95.0, bonusMax: 2.0 },
  zmdAmii: { min: 10.0, max: 6.0, bonusMax: 2.0 },
  zmdRip: { min: 10.0, max: 6.0, bonusMax: 2.0 },
  ztd: { min: 10.0, max: 6.0, bonusMax: 2.0 }
};

export default function UnifiedUploader() {
  const [activeModule, setActiveModule] = useState(null); 
  
  // Daba l'Cache ghadi y-stocki ga3 les fichiers isolés (7ta ZMD w ZTD)
  const [fichier1Cache, setFichier1Cache] = useState({}); 
  const [fichier2Data, setFichier2Data] = useState(null); 
  
  const [fileToUpload, setFileToUpload] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isGlobalDragging, setIsGlobalDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const fileInputRef = useRef(null);

  const [showConfigPanel, setShowConfigPanel] = useState(false);
  const [bonusConfig, setBonusConfig] = useState(INITIAL_CONFIG);

  const [showGlobalModal, setShowGlobalModal] = useState(false);

  const modules = {
    SACLI_OK: { id: 'SACLI_OK', category: 'RACC', label: 'SACLI OK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><path d="m9 12 2 2 4-4"></path></svg> },
    SARCLI_NOK: { id: 'SARCLI_NOK', category: 'RACC', label: 'SARCLI NOK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path><line x1="12" y1="9" x2="12" y2="13"></line></svg> },
    GEM_NOK: { id: 'GEM_NOK', category: 'RACC', label: 'GEM NOK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><polygon points="7.86 2 16.14 2 22 8.36 22 16.64 15.14 22 7.86 22 2 16.64 2 8.36 7.86 2"></polygon><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg> },
    TAUX_20J: { id: 'TAUX_20J', category: 'RACC', label: '1° RDV < 20J', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
    TNH: { id: 'TNH', category: 'RACC', label: 'TNH', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg> },
    PERF_RANG_1: { id: 'PERF_RANG_1', category: 'RACC', label: 'RANG 1 (PLP)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg> },
    HOTLINE_RANG_1: { id: 'HOTLINE_RANG_1', category: 'RACC', label: 'RANG 1 (HOTLINE)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg> },
    CONSTRUCTION_RANG_1: { id: 'CONSTRUCTION_RANG_1', category: 'RACC', label: 'RANG 1 (CONSTRUCT)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg> },
    PERF_RANG_2: { id: 'PERF_RANG_2', category: 'RACC', label: 'PERF RANG 2', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M7 13l5 5 5-5M7 6l5 5 5-5"/></svg> },
    ZMD_AMII: { id: 'ZMD_AMII', category: 'RACC', label: 'ZMD AMII', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"></path></svg> },
    ZMD_RIP: { id: 'ZMD_RIP', category: 'RACC', label: 'ZMD RIP', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path></svg> },
    ZTD: { id: 'ZTD', category: 'RACC', label: 'ZTD', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><rect x="4" y="2" width="16" height="20" rx="2" ry="2"></rect><path d="M9 22v-4h6v4"></path></svg> },
    SAV_PERF: { id: 'SAV_PERF', category: 'SAV', label: 'SAV PERF', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> },
    SAV_DELAI: { id: 'SAV_DELAI', category: 'SAV', label: 'SAV DÉLAI < 3J', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
    SECURISATION: { id: 'SECURISATION', category: 'SAV', label: 'SÉCURISATIONS', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg> },
    CCR: { id: 'CCR', category: 'SAV', label: 'CCR NON EXP', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg> },
    SATCLI_SAV: { id: 'SATCLI_SAV', category: 'SAV', label: 'SATCLI SAV', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg> }
  };

  const isFichier3Group = ["ZMD_AMII", "ZMD_RIP", "ZTD"].includes(activeModule);
  // Daba ga3 les uploads simples + les ZMD/ZTD taydiro appel l Endpoints isolés.
  const isIsolatedUpload = ["SACLI_OK", "SARCLI_NOK", "GEM_NOK", "TAUX_20J", "SAV_PERF", "SAV_DELAI", "SECURISATION", "CCR", "SATCLI_SAV", "ZMD_AMII", "ZMD_RIP", "ZTD"].includes(activeModule);
  const isFichier2Group = !isIsolatedUpload;
  
  const isMultiGroup = ["PERF_RANG_1", "HOTLINE_RANG_1", "CONSTRUCTION_RANG_1", "PERF_RANG_2"].includes(activeModule);
  const isBonusActive = ["PERF_RANG_1", "HOTLINE_RANG_1", "CONSTRUCTION_RANG_1", "PERF_RANG_2", "SACLI_OK", "SARCLI_NOK", "GEM_NOK", "TAUX_20J", "ZMD_AMII", "ZMD_RIP", "ZTD"].includes(activeModule);

  const getSingleConfigKey = () => {
    if (activeModule === 'SACLI_OK') return 'sacli';
    if (activeModule === 'SARCLI_NOK') return 'sarcli';
    if (activeModule === 'GEM_NOK') return 'gemNok';
    if (activeModule === 'TAUX_20J') return 'taux20j';
    return null;
  };
  const singleConfigKey = getSingleConfigKey();

  // ================= L'ALGORITHME: THE COMBO LOCK =================
  const computeBonus = (rawResult, conf, pdm) => {
    let baseBonus = 0;
    if (conf.min > conf.max) { // Logique Inversée
        if (rawResult <= conf.max) baseBonus = conf.bonusMax;
        else if (rawResult >= conf.min) baseBonus = 0;
        else baseBonus = conf.bonusMax * ((conf.min - rawResult) / (conf.min - conf.max));
    } else { // Logique Normale
        if (rawResult >= conf.max) baseBonus = conf.bonusMax;
        else if (rawResult <= conf.min) baseBonus = 0;
        else baseBonus = conf.bonusMax * ((rawResult - conf.min) / (conf.max - conf.min));
    }
    return baseBonus * (pdm / 100);
  };

  const getComboData = () => {
    const amii = fichier1Cache['ZMD_AMII']?.data;
    const rip = fichier1Cache['ZMD_RIP']?.data;
    const ztd = fichier1Cache['ZTD']?.data;

    // L'9fel: Khasshom ykonou b 3 wajdin bach y-t7el l'calcul
    const isComboUnlocked = amii && rip && ztd;

    const compute = (raw, key) => {
        if (!raw) return null;
        if (!isComboUnlocked) {
            return { ...raw, partDeMarche: 0, bonus: null, isComboUnlocked: false };
        }
        
        const totalDenum = amii.denum + rip.denum + ztd.denum;
        const pdm = totalDenum > 0 ? (raw.denum / totalDenum) * 100 : 0;
        const earnedBonus = computeBonus(raw.resultat, bonusConfig[key], pdm);

        return { 
          ...raw, 
          partDeMarche: pdm.toFixed(2), 
          bonus: earnedBonus.toFixed(2), 
          isComboUnlocked: true 
        };
    };

    return {
        ZMD_AMII: compute(amii, 'zmdAmii'),
        ZMD_RIP: compute(rip, 'zmdRip'),
        ZTD: compute(ztd, 'ztd')
    };
  };

  const comboData = getComboData();

  const getCurrentResult = () => {
    if (!activeModule) return null;
    if (isFichier3Group) {
        const d = comboData[activeModule];
        if (d) return { title: `Rapport : ${modules[activeModule].label}`, data: d };
        return null;
    }
    if (isIsolatedUpload && fichier1Cache[activeModule]) return fichier1Cache[activeModule];
    if (isFichier2Group && fichier2Data) {
      if (activeModule === "TNH" && fichier2Data.tnh) return { title: "Rapport : TNH", data: fichier2Data.tnh };
      if (activeModule === "PERF_RANG_1" && fichier2Data.perfRang1) return { title: "Rapport : RANG 1 (PLP)", data: fichier2Data.perfRang1 };
      if (activeModule === "HOTLINE_RANG_1" && fichier2Data.hotlineRang1) return { title: "Rapport : RANG 1 (HOTLINE)", data: fichier2Data.hotlineRang1 };
      if (activeModule === "CONSTRUCTION_RANG_1" && fichier2Data.constructionRang1) return { title: "Rapport : RANG 1 (CONSTRUCT)", data: fichier2Data.constructionRang1 };
      if (activeModule === "PERF_RANG_2" && fichier2Data.perfRang2) return { title: "Rapport : PERF RANG 2", data: fichier2Data.perfRang2 };
    }
    return null;
  };

  const currentResult = getCurrentResult();
  const handleTabChange = (moduleId) => {
    setActiveModule(moduleId); setFileToUpload(null); setError("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleConfigChange = (process, zone, minOrMax, value) => {
    setBonusConfig(prev => ({
      ...prev,
      [process]: { ...prev[process], [zone]: { ...prev[process][zone], [minOrMax]: parseFloat(value) || 0 } }
    }));
  };

  const handleSingleConfigChange = (process, key, value) => {
    setBonusConfig(prev => ({ ...prev, [process]: { ...prev[process], [key]: parseFloat(value) || 0 } }));
  };

  const handleFileChange = (e) => { if (e.target.files && e.target.files[0]) { setFileToUpload(e.target.files[0]); setError(""); } };
  const removeFileToUpload = () => { setFileToUpload(null); if (fileInputRef.current) fileInputRef.current.value = ""; };
  
  const handleResetData = () => {
    if (isIsolatedUpload) {
      setFichier1Cache(prev => { const newC = {...prev}; delete newC[activeModule]; return newC; });
    } else {
      setFichier2Data(null); 
    }
    setFileToUpload(null);
  };

  const handleAnalyze = async () => {
    if (!fileToUpload || !activeModule) return setError("Veuillez injecter un fichier d'abord.");
    setLoading(true); setError("");
    
    // L'appel l'Endpoint
    let endpoint = isIsolatedUpload ? API_ENDPOINTS_ISOLATED[activeModule] : "/api/v1/dashboard/fichier2";
    const formData = new FormData(); formData.append('file', fileToUpload); formData.append('config', JSON.stringify(bonusConfig));

    try {
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7623';
      const res = await fetch(`${baseUrl}${endpoint}`, { method: 'POST', body: formData });
      if (!res.ok) throw new Error(await res.text() || `Erreur serveur ${res.status}`);
      const fullData = await res.json();
      
      if (isIsolatedUpload) {
        setFichier1Cache(prev => ({ ...prev, [activeModule]: { title: `Rapport : ${modules[activeModule].label}`, data: fullData } }));
      } else {
        setFichier2Data(fullData);
      }
      setFileToUpload(null);
    } catch (err) { setError(`Échec : ${err.message}`); } finally { setLoading(false); }
  };

  const extractMultiTotal = (multiData) => {
    if (!multiData) return null;
    const num = (multiData.groupA?.num || 0) + (multiData.groupB?.num || 0) + (multiData.groupC?.num || 0);
    const denum = (multiData.groupA?.denum || 0) + (multiData.groupB?.denum || 0) + (multiData.groupC?.denum || 0);
    const bonus = (multiData.groupA?.bonus || 0) + (multiData.groupB?.bonus || 0) + (multiData.groupC?.bonus || 0);
    return { num, denum, resultat: denum > 0 ? ((num / denum) * 100).toFixed(2) : "0.00", bonus: bonus.toFixed(2) };
  };

  const globalRows = Object.values(modules).map(mod => {
    let d = null;
    if (["SACLI_OK", "SARCLI_NOK", "GEM_NOK", "TAUX_20J", "SAV_PERF", "SAV_DELAI", "SECURISATION", "CCR", "SATCLI_SAV"].includes(mod.id)) { 
      d = fichier1Cache[mod.id]?.data; 
    }
    else if (["ZMD_AMII", "ZMD_RIP", "ZTD"].includes(mod.id)) {
      d = comboData[mod.id];
    }
    else if (mod.id === "TNH") d = fichier2Data?.tnh;
    else if (mod.id === "PERF_RANG_1") d = extractMultiTotal(fichier2Data?.perfRang1);
    else if (mod.id === "HOTLINE_RANG_1") d = extractMultiTotal(fichier2Data?.hotlineRang1);
    else if (mod.id === "CONSTRUCTION_RANG_1") d = extractMultiTotal(fichier2Data?.constructionRang1);
    else if (mod.id === "PERF_RANG_2") d = extractMultiTotal(fichier2Data?.perfRang2);
    return { ...mod, data: d };
  });

  const showConfigButton = isFichier2Group || isFichier3Group || singleConfigKey !== null;
  const singleGridClass = isBonusActive && isFichier3Group ? "grid5" : isBonusActive ? "grid4" : "grid3";

  // Icons
  const IconNum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>;
  const IconDenum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>;
  const IconPie = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path><path d="M22 12A10 10 0 0 0 12 2v10z"></path></svg>;
  const IconBonus = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>;
  const IconResult = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>;
  const IconSettings = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>;

  const getZoneLabel = (zoneLetter) => {
    if (activeModule === "PERF_RANG_1") return `PLP ZONE ${zoneLetter}`;
    if (activeModule === "HOTLINE_RANG_1") return `HOTLINE ZONE ${zoneLetter}`;
    if (activeModule === "CONSTRUCTION_RANG_1") return `CONSTRUCT ZONE ${zoneLetter}`;
    if (activeModule === "PERF_RANG_2") return `GLOBAL ZONE ${zoneLetter}`;
    return `ZONE ${zoneLetter}`;
  };

  const formatDataForUI = () => {
    const raw = currentResult?.data;
    if (!raw) return null;
    if (!isMultiGroup) return raw; 
    return {
      groupA: raw.groupA || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
      groupB: raw.groupB || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
      groupC: raw.groupC || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
    };
  };

  const uiData = formatDataForUI();

  // Design helpers for locked UI
  const pdmValueDisplay = isFichier3Group && uiData && !uiData.isComboUnlocked ? "🔒 En attente" : `${uiData?.partDeMarche || 0}%`;
  const bonusValueDisplay = isFichier3Group && uiData && !uiData.isComboUnlocked ? "🔒 Bloqué" : `+${uiData?.bonus || 0}%`;

  useEffect(() => {
    const handleGlobalDragOver = (e) => { e.preventDefault(); if (!fileToUpload && activeModule && !currentResult) setIsGlobalDragging(true); };
    const handleGlobalDragLeave = (e) => { e.preventDefault(); if (e.clientX === 0 && e.clientY === 0) setIsGlobalDragging(false); };
    const handleGlobalDrop = (e) => { e.preventDefault(); setIsGlobalDragging(false); };

    window.addEventListener("dragover", handleGlobalDragOver);
    window.addEventListener("dragleave", handleGlobalDragLeave);
    window.addEventListener("drop", handleGlobalDrop);

    return () => {
      window.removeEventListener("dragover", handleGlobalDragOver);
      window.removeEventListener("dragleave", handleGlobalDragLeave);
      window.removeEventListener("drop", handleGlobalDrop);
    };
  }, [fileToUpload, activeModule, currentResult]);

  return (
    <>
      <style dangerouslySetInnerHTML={{__html: `
        body, html { background-color: #0b0f19 !important; margin: 0; padding: 0; overflow-x: hidden; font-family: system-ui, -apple-system, sans-serif; }
        .grid3 { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-top: 15px; }
        .grid4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-top: 15px; }
        .grid5 { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-top: 15px; }
        
        .cyberDropzone {
          border: 2px dashed rgba(59, 130, 246, 0.3); border-radius: 12px; padding: 40px 20px;
          background: rgba(30, 41, 59, 0.2); position: relative; overflow: hidden; cursor: pointer; transition: all 0.3s ease;
        }
        .cyberDropzone:hover { border-color: #3b82f6; background: rgba(59, 130, 246, 0.05); }
        .cyberDropzone::before {
          content: ''; position: absolute; width: 100%; height: 2px; background: linear-gradient(90deg, transparent, #3b82f6, transparent);
          top: 0; left: 0; animation: laserScan 2.5s linear infinite; opacity: 0.4;
        }
        @keyframes laserScan { 0% { top: 0%; } 50% { top: 100%; } 100% { top: 0%; } }
        
        .categorySection { margin-bottom: 25px; }
        .categoryLabel { font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
        .categoryLabel::after { content: ''; flex-grow: 1; height: 1px; background: rgba(255,255,255,0.05); }

        .fabGlobalBtn {
          position: fixed; bottom: 30px; right: 30px; z-index: 1000;
          background: linear-gradient(135deg, #1e293b, #0f172a); color: #3b82f6;
          border: 1px solid rgba(59, 130, 246, 0.3); border-radius: 30px; padding: 14px 24px;
          font-weight: 700; font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 10px;
          box-shadow: 0 10px 30px rgba(0,0,0,0.5); transition: all 0.3s;
        }
        .fabGlobalBtn:hover { transform: translateY(-2px); border-color: #3b82f6; color: #fff; box-shadow: 0 0 20px rgba(59, 130, 246, 0.2); }

        .lockBanner {
          background: rgba(245, 158, 11, 0.08); border: 1px solid rgba(245, 158, 11, 0.25);
          padding: 16px 20px; border-radius: 12px; color: #fcd34d; margin-bottom: 24px;
          display: flex; alignItems: center; gap: 15px; animation: cyberFadeIn 0.5s ease;
          box-shadow: inset 0 0 20px rgba(245, 158, 11, 0.02);
        }
      `}} />

      <button className="fabGlobalBtn" onClick={() => setShowGlobalModal(true)}>
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
        Vue Globale
      </button>

      {showGlobalModal && <GlobalViewModal onClose={() => setShowGlobalModal(false)} globalRows={globalRows} />}

      <div className={styles.mainWrapper} style={{ minHeight: '100vh', paddingBottom: '100px', position: 'relative', zIndex: 1 }}>
        <ShatteredGlass />
        <InteractiveBackground />

        <div className={`${styles.uiTriggerZone} ${isMultiGroup && currentResult ? styles.expandedZone : ''}`}>
          <div className={styles.glassContainer} style={{ background: 'rgba(15, 23, 42, 0.65)', border: '1px solid rgba(255,255,255,0.05)' }}>
            
            <div className={styles.header}>
              <div className={styles.logoWrapper} style={{ background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline></svg>
              </div>
              <h1 className={styles.title} style={{ color: '#fff' }}>Kyntus OS</h1>
              <p className={styles.subtitle} style={{ color: '#64748b' }}>Intelligence Qualité & Traitement Automatisé</p>
            </div>

            <div className={styles.moduleSelectorWrapper} style={{ display: 'block' }}>
              <div className="categorySection">
                <div className="categoryLabel">Flux Qualité RACC</div>
                <div className={styles.moduleSelector} style={{ flexWrap: 'wrap', justifyContent: 'flex-start', gap: '8px' }}>
                  {Object.values(modules).filter(mod => mod.category === 'RACC').map((mod) => (
                    <button key={mod.id} onClick={() => handleTabChange(mod.id)} className={`${styles.tabBtn} ${activeModule === mod.id ? styles.activeTab : ''}`} style={{ borderRadius: '8px', background: activeModule === mod.id ? '#3b82f6' : 'rgba(30,41,59,0.4)', color: activeModule === mod.id ? '#fff' : '#94a3b8' }}>
                      <span className={styles.tabIcon}>{mod.icon}</span> {mod.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="categorySection" style={{ marginBottom: 0 }}>
                <div className="categoryLabel" style={{ color: '#8b5cf6' }}>Flux Qualité SAV</div>
                <div className={styles.moduleSelector} style={{ flexWrap: 'wrap', justifyContent: 'flex-start', gap: '8px' }}>
                  {Object.values(modules).filter(mod => mod.category === 'SAV').map((mod) => (
                    <button key={mod.id} onClick={() => handleTabChange(mod.id)} className={`${styles.tabBtn} ${activeModule === mod.id ? styles.activeTab : ''}`} style={{ borderRadius: '8px', background: activeModule === mod.id ? '#8b5cf6' : 'rgba(30,41,59,0.4)', color: activeModule === mod.id ? '#fff' : '#94a3b8' }}>
                      <span className={styles.tabIcon}>{mod.icon}</span> {mod.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
            
            {error && <div className={styles.errorAlert} style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', color: '#ef4444', marginTop: '20px' }}>{error}</div>}

            <div className={styles.contentGrid}>
              {!currentResult && (
                <div className={styles.uploadSection}>
                  {showConfigButton && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '15px' }}>
                      <button onClick={() => setShowConfigPanel(!showConfigPanel)} className={`configBtn ${showConfigPanel ? 'active' : ''}`} style={{ background: 'rgba(30,41,59,0.5)', border: '1px solid rgba(255,255,255,0.05)', color: '#94a3b8' }}>
                        <span style={{ width: '16px', display: 'flex' }}><IconSettings/></span>
                        {showConfigPanel ? 'Fermer la matrice' : 'Paramétrer la Matrice'}
                      </button>
                    </div>
                  )}

                  {showConfigPanel && showConfigButton && (
                    <div className="configPanel" style={{ background: '#111726', borderColor: 'rgba(255,255,255,0.03)' }}>
                      <h4 style={{ margin: '0 0 15px 0', color: '#fff', fontSize: '14px', fontWeight: '700', letterSpacing: '0.5px' }}>
                        Configuration des seuils cibles 
                        {(activeModule === 'GEM_NOK' || isFichier3Group) && <span style={{ color: '#ef4444', fontSize: '11px', marginLeft: '10px', padding: '2px 6px', background: 'rgba(239,68,68,0.1)', borderRadius: '4px' }}>Logique Inversée</span>}
                      </h4>
                      
                      {isFichier2Group && (
                        <table className="configTable">
                          <thead>
                            <tr><th>Métrique</th><th colSpan="2" style={{ color: '#3b82f6' }}>ZONE A</th><th colSpan="2" style={{ color: '#8b5cf6' }}>ZONE B</th><th colSpan="2" style={{ color: '#10b981' }}>ZONE C</th></tr>
                          </thead>
                          <tbody>
                            {['plp', 'hotline', 'construction', 'rang2'].map((process) => (
                              <tr key={process}>
                                <td style={{ color: '#94a3b8', fontWeight: '700' }}>{process.toUpperCase()}</td>
                                {['a', 'b', 'c'].map(zone => (
                                  <React.Fragment key={`${process}-${zone}`}>
                                    <td><input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[process][zone].min} onChange={(e) => handleConfigChange(process, zone, 'min', e.target.value)} /></td>
                                    <td><input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[process][zone].max} onChange={(e) => handleConfigChange(process, zone, 'max', e.target.value)} /></td>
                                  </React.Fragment>
                                ))}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}

                      {isFichier3Group && (
                        <table className="configTable">
                          <thead><tr><th>Métrique SAV</th><th style={{ color: '#ef4444' }}>Seuil MIN</th><th style={{ color: '#10b981' }}>Seuil MAX</th><th style={{ color: '#f59e0b' }}>Bonus MAX</th></tr></thead>
                          <tbody>
                            {[{key: 'zmdAmii', label: 'ZMD AMII'}, {key: 'zmdRip', label: 'ZMD RIP'}, {key: 'ztd', label: 'ZTD'}].map((item) => (
                              <tr key={item.key}>
                                <td style={{ color: '#fff', fontWeight: '700' }}>{item.label}</td>
                                <td><input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[item.key].min} onChange={(e) => handleSingleConfigChange(item.key, 'min', e.target.value)} /></td>
                                <td><input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[item.key].max} onChange={(e) => handleSingleConfigChange(item.key, 'max', e.target.value)} /></td>
                                <td><input type="number" step="0.1" className="configInput" style={{ background: '#090d16', borderColor: 'rgba(245,158,11,0.3)', color: '#f59e0b' }} value={bonusConfig[item.key].bonusMax} onChange={(e) => handleSingleConfigChange(item.key, 'bonusMax', e.target.value)} /></td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}

                      {singleConfigKey && !isFichier3Group && (
                        <div style={{ display: 'flex', gap: '30px', justifyContent: 'center' }}>
                          <div>
                            <label style={{ display: 'block', fontSize: '11px', color: '#64748b', marginBottom: '6px' }}>Seuil MIN</label>
                            <input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[singleConfigKey].min} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'min', e.target.value)} />
                          </div>
                          <div>
                            <label style={{ display: 'block', fontSize: '11px', color: '#64748b', marginBottom: '6px' }}>Seuil MAX</label>
                            <input type="number" step="0.1" className="configInput" style={{ background: '#090d16', border: '1px solid rgba(255,255,255,0.05)', color: '#fff' }} value={bonusConfig[singleConfigKey].max} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'max', e.target.value)} />
                          </div>
                          <div>
                            <label style={{ display: 'block', fontSize: '11px', color: '#f59e0b', marginBottom: '6px' }}>Bonus MAX</label>
                            <input type="number" step="0.1" className="configInput" style={{ background: '#090d16', borderColor: 'rgba(245,158,11,0.3)', color: '#f59e0b' }} value={bonusConfig[singleConfigKey].bonusMax} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'bonusMax', e.target.value)} />
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  {!activeModule ? (
                    <div className={styles.placeholderMessage} style={{ color: '#475569' }}>Sélectionnez une métrique opérationnelle pour charger le scanner.</div>
                  ) : !fileToUpload ? (
                    <div className="cyberDropzone" onClick={() => fileInputRef.current?.click()}>
                      <input type="file" accept=".csv, .xlsx, .xls" onChange={handleFileChange} ref={fileInputRef} className={styles.hiddenInput} />
                      <div style={{ color: '#94a3b8' }}>
                        <h3 style={{ color: '#fff', margin: '0 0 8px 0', fontSize: '16px' }}>Charger la matrice brute</h3>
                        <p style={{ margin: 0, fontSize: '13px', color: '#475569' }}>Formats acceptés: CSV, XLSX, XLS</p>
                      </div>
                    </div>
                  ) : (
                    <div className={styles.fileSelectedWrapper}>
                      <div className={styles.fileSelectedCard} style={{ background: 'rgba(255,255,255,0.02)', borderColor: 'rgba(255,255,255,0.05)' }}>
                        <span style={{ color: '#fff', fontWeight: '600' }}>{fileToUpload.name}</span>
                        <button onClick={removeFileToUpload} className={styles.removeBtn} style={{ color: '#ef4444' }}>Retirer</button>
                      </div>
                      <button onClick={handleAnalyze} disabled={loading} className={styles.actionBtn} style={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)', color: '#fff', width: '100%', padding: '14px', borderRadius: '8px', fontWeight: '700', border: 'none', cursor: 'pointer', marginTop: '15px' }}>
                        {loading ? 'Analyse matricielle en cours...' : 'Lancer l\'Analyse'}
                      </button>
                    </div>
                  )}
                </div>
              )}

              {currentResult && uiData && (
                <div className={styles.resultSection}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h3 className={styles.resultTitle} style={{ color: '#fff', margin: 0 }}>{currentResult.title}</h3>
                    <button onClick={handleResetData} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '13px', fontWeight: '600', textDecoration: 'underline' }}>Réinitialiser</button>
                  </div>

                  {/* L'BANNER DYAL THE COMBO LOCK */}
                  {isFichier3Group && !uiData.isComboUnlocked && (
                    <div className="lockBanner">
                       <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                       <div>
                          <h4 style={{ margin: '0 0 4px 0', fontSize: '16px', fontWeight: '800' }}>Mécanisme Sécurisé : Combo Incomplet</h4>
                          <p style={{ margin: 0, fontSize: '13px', color: '#d97706', fontWeight: '500' }}>
                            Veuillez injecter les 3 fichiers isolés (ZMD AMII, ZMD RIP, ZTD) pour que le système puisse débloquer et consolider la Part de Marché ainsi que le Bonus.
                          </p>
                       </div>
                    </div>
                  )}
                  
                  {isMultiGroup ? (
                    <div className={styles.multiGroupResult}>
                      {['groupA', 'groupB', 'groupC'].map((gKey, idx) => (
                        <div className={styles.groupContainer} key={gKey} style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid rgba(255,255,255,0.03)', padding: '20px', borderRadius: '12px', marginBottom: '20px' }}>
                          <h4 className={styles.groupTitle} style={{ color: '#fff', margin: '0 0 15px 0' }}>Zone Groupe {String.fromCharCode(65 + idx)}</h4>
                          <div className={isBonusActive ? "grid5" : "grid4"}>
                            <ResultCard label="NUM" value={uiData[gKey].num} icon={<IconNum/>} />
                            <ResultCard label="DENUM" value={uiData[gKey].denum} icon={<IconDenum/>} />
                            <ResultCard label="Poids global" value={`${uiData[gKey].partDeMarche || 0}%`} icon={<IconPie/>} />
                            <ResultCard highlight={true} label="Taux brut" value={`${uiData[gKey].resultat}%`} icon={<IconResult/>} />
                            {isBonusActive && <ResultCard highlight={true} label="Bonus" value={`+${uiData[gKey].bonus || 0}%`} icon={<IconBonus/>} />}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className={singleGridClass}>
                      <ResultCard label="Numérateur" value={uiData.num} icon={<IconNum/>} />
                      <ResultCard label="Dénominateur" value={uiData.denum} icon={<IconDenum/>} />
                      
                      {isFichier3Group && <ResultCard label="Poids global" value={pdmValueDisplay} icon={<IconPie/>} />}
                      
                      <ResultCard highlight={true} label="Taux d'efficacité" value={`${uiData.resultat}%`} icon={<IconResult/>} />
                      
                      {isBonusActive && <ResultCard highlight={true} label="Bonus acquis" value={bonusValueDisplay} icon={<IconBonus/>} />}
                    </div>
                  )}

                  {/* L'Tableau kayakhod uiData bla manbeddlou fih, li mbloki fih partDeMarche=0 w bonus=null ghayban khawi */}
                  <MasterTable data={uiData} activeModule={activeModule} />
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}