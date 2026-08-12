"use client";

import React, { useState, useRef, useEffect } from "react";
import InteractiveBackground from "../threejs/InteractiveBackground";
import ShatteredGlass from "./ShatteredGlass";
import ResultCard from "./ResultCard";
import MasterTable from "./MasterTable";
import GlobalViewModal from "./GlobalViewModal";
import styles from "../styles/unified.module.css";

const API_ENDPOINTS_ISOLATED = {
  SACLI_OK: "/api/v1/excel/sacli/analyze",
  SARCLI_NOK: "/api/v1/excel/sarcli/analyze",
  GEM_NOK: "/api/v1/excel/gemnok/analyze",
  TAUX_20J: "/api/v1/excel/taux20j/analyze",
  ZMD_AMII: "/api/v1/excel/zmdamii/analyze",
  ZMD_RIP: "/api/v1/excel/zmdrip/analyze",
  ZTD: "/api/v1/excel/ztd/analyze",
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
  sacli: { min: 85.0, max: 95.0, bonusMax: 2.0 },
  sarcli: { min: 30.0, max: 55.0, bonusMax: 1.0 },
  gemNok: { min: 5.0, max: 2.0, bonusMax: 2.0 },
  taux20j: { min: 80.0, max: 95.0, bonusMax: 2.0 },
  zmdAmii: { min: 10.0, max: 6.0, bonusMax: 2.0 },
  zmdRip: { min: 10.0, max: 6.0, bonusMax: 2.0 },
  ztd: { min: 10.0, max: 6.0, bonusMax: 2.0 }
};

export default function UnifiedUploader() {
  const [activeModule, setActiveModule] = useState(null); 
  
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

  // L'FIX HWA HNA: State l'Mois, l'Année, w l'Département
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedDept, setSelectedDept] = useState("GLOBAL");

  const modules = {
    SACLI_OK: { id: 'SACLI_OK', category: 'RACC', label: 'SACLI OK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"></path><path d="m9 12 2 2 4-4"></path></svg> },
    SARCLI_NOK: { id: 'SARCLI_NOK', category: 'RACC', label: 'SARCLI NOK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg> },
    GEM_NOK: { id: 'GEM_NOK', category: 'RACC', label: 'GEM NOK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg> },
    TAUX_20J: { id: 'TAUX_20J', category: 'RACC', label: '1° RDV < 20J', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
    TNH: { id: 'TNH', category: 'RACC', label: 'TNH', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg> },
    PERF_RANG_1: { id: 'PERF_RANG_1', category: 'RACC', label: 'RANG 1 (PLP)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg> },
    HOTLINE_RANG_1: { id: 'HOTLINE_RANG_1', category: 'RACC', label: 'RANG 1 (HOTLINE)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg> },
    CONSTRUCTION_RANG_1: { id: 'CONSTRUCTION_RANG_1', category: 'RACC', label: 'RANG 1 (CONSTRUCT)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg> },
    PERF_RANG_2: { id: 'PERF_RANG_2', category: 'RACC', label: 'PERF RANG 2', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M7 13l5 5 5-5M7 6l5 5 5-5"/></svg> },
    ZMD_AMII: { id: 'ZMD_AMII', category: 'RACC', label: 'ZMD AMII', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"></path></svg> },
    ZMD_RIP: { id: 'ZMD_RIP', category: 'RACC', label: 'ZMD RIP', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path></svg> },
    ZTD: { id: 'ZTD', category: 'RACC', label: 'ZTD', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><rect x="4" y="2" width="16" height="20" rx="2" ry="2"></rect><path d="M9 22v-4h6v4"></path><path d="M8 6h.01"></path><path d="M16 6h.01"></path><path d="M12 6h.01"></path><path d="M12 10h.01"></path><path d="M12 14h.01"></path><path d="M16 10h.01"></path><path d="M16 14h.01"></path><path d="M8 10h.01"></path><path d="M8 14h.01"></path></svg> },
    SAV_PERF: { id: 'SAV_PERF', category: 'SAV', label: 'SAV PERF', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> },
    SAV_DELAI: { id: 'SAV_DELAI', category: 'SAV', label: 'SAV DÉLAI < 3J', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
    SECURISATION: { id: 'SECURISATION', category: 'SAV', label: 'SÉCURISATIONS', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg> },
    CCR: { id: 'CCR', category: 'SAV', label: 'CCR NON EXP', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg> },
    SATCLI_SAV: { id: 'SATCLI_SAV', category: 'SAV', label: 'SATCLI SAV', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg> }
  };

  const isFichier3Group = ["ZMD_AMII", "ZMD_RIP", "ZTD"].includes(activeModule);
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

  const computeBonus = (rawResult, conf, pdm) => {
    let baseBonus = 0;
    if (conf.min > conf.max) { 
        if (rawResult <= conf.max) baseBonus = conf.bonusMax;
        else if (rawResult >= conf.min) baseBonus = 0;
        else baseBonus = conf.bonusMax * ((conf.min - rawResult) / (conf.min - conf.max));
    } else { 
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

    const isComboUnlocked = !!(amii && rip && ztd);

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
          partDeMarche: Number(pdm.toFixed(2)), 
          bonus: Number(earnedBonus.toFixed(2)), 
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
      if (activeModule === "TNH" && fichier2Data.tnh) return { title: "Rapport : TNH (CR Delai)", data: fichier2Data.tnh };
      if (activeModule === "PERF_RANG_1" && fichier2Data.perfRang1) return { title: "Rapport : PERF RANG 1 (PLP)", data: fichier2Data.perfRang1 };
      if (activeModule === "HOTLINE_RANG_1" && fichier2Data.hotlineRang1) return { title: "Rapport : PERF RANG 1 (HOTLINE)", data: fichier2Data.hotlineRang1 };
      if (activeModule === "CONSTRUCTION_RANG_1" && fichier2Data.constructionRang1) return { title: "Rapport : PERF RANG 1 (CONSTRUCT)", data: fichier2Data.constructionRang1 };
      if (activeModule === "PERF_RANG_2" && fichier2Data.perfRang2) return { title: "Rapport : PERF RANG 2 (TOUS PROCESS)", data: fichier2Data.perfRang2 };
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
      [process]: {
        ...prev[process],
        [zone]: { ...prev[process][zone], [minOrMax]: parseFloat(value) || 0 }
      }
    }));
  };

  const handleSingleConfigChange = (process, key, value) => {
    setBonusConfig(prev => ({
      ...prev,
      [process]: { ...prev[process], [key]: parseFloat(value) || 0 }
    }));
  };

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

  const handleDragOver = (e) => { e.preventDefault(); setIsDragging(true); };
  const handleDragLeave = (e) => { e.preventDefault(); setIsDragging(false); };
  const handleDrop = (e) => {
    e.preventDefault(); setIsDragging(false); setIsGlobalDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) { setFileToUpload(e.dataTransfer.files[0]); setError(""); }
  };
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) { setFileToUpload(e.target.files[0]); setError(""); }
  };
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
    
    let endpoint = isIsolatedUpload ? API_ENDPOINTS_ISOLATED[activeModule] : "/api/v1/dashboard/fichier2";

    const formData = new FormData(); 
    formData.append('file', fileToUpload);
    formData.append('config', JSON.stringify(bonusConfig)); 
    
    // L'FIX HWA HNA: Kan-siftou l'Mois w l'Année f l'API
    formData.append('month', selectedMonth);
    formData.append('year', selectedYear);

    try {
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7623';
      const res = await fetch(`${baseUrl}${endpoint}`, { method: 'POST', body: formData });
      
      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || `Erreur serveur (Status: ${res.status})`);
      }
      
      const fullData = await res.json();
      
      if (isIsolatedUpload) {
        const title = `Rapport : ${modules[activeModule].label}`;
        
        // L'FIX HWA HNA: Kan-gériw l'format jdid dyal SACLI (li fih "details")
        let dataToStore = fullData;
        let depts = [];
        
        if (fullData.details) {
          dataToStore = fullData.details;
          depts = Object.keys(fullData.details).sort((a, b) => {
            if (a === "GLOBAL") return -1;
            if (b === "GLOBAL") return 1;
            return a.localeCompare(b);
          });
        }

        setFichier1Cache(prev => ({ 
          ...prev, 
          [activeModule]: { title, data: dataToStore, departments: depts } 
        }));
        setSelectedDept("GLOBAL"); // Reset l'dropdown mli kayt-uploada fichier jdid

      } else {
        setFichier2Data(fullData);
      }
      
      setFileToUpload(null); 
    } catch (err) { setError(`Échec : ${err.message}`); } 
    finally { setLoading(false); }
  };

  const extractMultiTotal = (multiData) => {
    if (!multiData) return null;
    const num = (multiData.groupA?.num || 0) + (multiData.groupB?.num || 0) + (multiData.groupC?.num || 0);
    const denum = (multiData.groupA?.denum || 0) + (multiData.groupB?.denum || 0) + (multiData.groupC?.denum || 0);
    const bonus = (multiData.groupA?.bonus || 0) + (multiData.groupB?.bonus || 0) + (multiData.groupC?.bonus || 0);
    const resultat = denum > 0 ? ((num / denum) * 100).toFixed(2) : "0.00";
    return { num, denum, resultat, bonus: bonus.toFixed(2) };
  };

  const getGlobalDataRows = () => {
    return Object.values(modules).map(mod => {
      let dataToDisplay = null;
      if (["SACLI_OK", "SARCLI_NOK", "GEM_NOK", "TAUX_20J", "SAV_PERF", "SAV_DELAI", "SECURISATION", "CCR", "SATCLI_SAV"].includes(mod.id)) {
        if (fichier1Cache[mod.id]) {
          // Ila kan fih details (b7al SACLI), n-affichiw l'GLOBAL f l'Vue Globale
          dataToDisplay = fichier1Cache[mod.id].departments ? fichier1Cache[mod.id].data["GLOBAL"] : fichier1Cache[mod.id].data;
        }
      } else if (["ZMD_AMII", "ZMD_RIP", "ZTD"].includes(mod.id)) {
        dataToDisplay = comboData[mod.id];
      } else if (mod.id === "TNH") {
        dataToDisplay = fichier2Data?.tnh;
      } else if (mod.id === "PERF_RANG_1") {
        dataToDisplay = extractMultiTotal(fichier2Data?.perfRang1);
      } else if (mod.id === "HOTLINE_RANG_1") {
        dataToDisplay = extractMultiTotal(fichier2Data?.hotlineRang1);
      } else if (mod.id === "CONSTRUCTION_RANG_1") {
        dataToDisplay = extractMultiTotal(fichier2Data?.constructionRang1);
      } else if (mod.id === "PERF_RANG_2") {
        dataToDisplay = extractMultiTotal(fichier2Data?.perfRang2);
      }
      return { ...mod, data: dataToDisplay };
    });
  };

  const globalRows = getGlobalDataRows();

  const IconNum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>;
  const IconDenum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>;
  const IconPie = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path><path d="M22 12A10 10 0 0 0 12 2v10z"></path></svg>;
  const IconBonus = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>;
  const IconResult = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>;
  const IconSettings = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>;

  const getZoneLabel = (zoneLetter) => {
    if (activeModule === "PERF_RANG_1") return `PLP ZONE ${zoneLetter}`;
    if (activeModule === "HOTLINE_RANG_1") return `HOTLINE ZONE ${zoneLetter}`;
    if (activeModule === "CONSTRUCTION_RANG_1") return `CONSTRUCT ZONE ${zoneLetter}`;
    if (activeModule === "PERF_RANG_2") return `GLOBAL ZONE ${zoneLetter}`;
    return `ZONE ${zoneLetter}`;
  };

  const formatDataForUI = () => {
    const res = currentResult;
    if (!res) return null;

    // L'FIX HWA HNA: Ila kan l'format jdid dyal SACLI (fih departments)
    if (res.departments && res.data[selectedDept]) {
      return res.data[selectedDept];
    }

    // L'format l9dim
    const raw = res.data;
    if (!isMultiGroup) return raw; 
    return {
      groupA: raw.groupA || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
      groupB: raw.groupB || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
      groupC: raw.groupC || { num: 0, denum: 0, resultat: 0, partDeMarche: 0, bonus: 0 },
    };
  };

  const uiData = formatDataForUI();
  const showConfigButton = isFichier2Group || isFichier3Group || singleConfigKey !== null;
  const singleGridClass = isBonusActive && isFichier3Group ? "grid5" : isBonusActive ? "grid4" : "grid3";

  const pdmValueDisplay = isFichier3Group && uiData && !uiData.isComboUnlocked ? "🔒" : `${Number(uiData?.partDeMarche || 0).toFixed(2)}%`;
  const bonusValueDisplay = isFichier3Group && uiData && !uiData.isComboUnlocked ? "🔒" : `+${Number(uiData?.bonus || 0).toFixed(2)}%`;

  return (
    <>
      <style dangerouslySetInnerHTML={{__html: `
        body, html { background-color: #ffffff !important; margin: 0; padding: 0; overflow-x: hidden; overflow-y: auto !important; }
        canvas { position: fixed !important; top: 0; left: 0; z-index: -1; }
        .grid3 { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-top: 15px; }
        .grid4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-top: 15px; }
        .grid5 { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 16px; margin-top: 15px; }
        
        .configPanel { 
          background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(10px);
          border: 1px solid rgba(226, 232, 240, 0.8); border-radius: 12px; 
          padding: 24px; margin-bottom: 24px;
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); animation: slideDown 0.3s ease-out;
        }
        @keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
        
        @keyframes goldPulse { 0% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.4); } 70% { box-shadow: 0 0 0 10px rgba(245, 158, 11, 0); } 100% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0); } }
        .gold-pulse-bg { animation: goldPulse 2s infinite; }
        
        .configTable { width: 100%; border-collapse: separate; border-spacing: 0 10px; text-align: center; }
        .configTable th { padding: 10px; font-size: 13px; color: #64748b; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #f1f5f9; }
        .configTable td { padding: 8px; }
        
        .configInput { width: 70px; padding: 10px; border: 1px solid #cbd5e1; border-radius: 8px; text-align: center; font-weight: 700; color: #0f172a; background: #f8fafc; transition: all 0.2s ease; }
        .configInput:focus { outline: none; border-color: #3b82f6; background: #fff; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); }
        .configBtn { display: flex; alignItems: center; gap: 8px; background: #ffffff; border: 1px solid #cbd5e1; padding: 10px 20px; border-radius: 8px; cursor: pointer; color: #334155; font-weight: 600; transition: all 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.02); }
        .configBtn:hover { background: #f8fafc; border-color: #94a3b8; transform: translateY(-1px); }
        .configBtn.active { background: #eff6ff; border-color: #3b82f6; color: #2563eb; }

        .categorySection { margin-bottom: 25px; }
        .categoryLabel { font-size: 14px; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
        .categoryLabel::after { content: ''; flex-grow: 1; height: 1px; background: #e2e8f0; }

        .fabGlobalBtn {
          position: fixed; bottom: 30px; right: 30px; z-index: 1000;
          background: linear-gradient(135deg, #3b82f6, #2563eb); color: white;
          border: none; border-radius: 50px; padding: 15px 25px;
          font-weight: bold; font-size: 16px; cursor: pointer;
          display: flex; align-items: center; gap: 10px;
          box-shadow: 0 10px 25px rgba(59, 130, 246, 0.4);
          transition: transform 0.3s, box-shadow 0.3s;
        }
        .fabGlobalBtn:hover { transform: translateY(-3px) scale(1.02); box-shadow: 0 15px 35px rgba(59, 130, 246, 0.5); }

        .lockBannerLight {
          background-color: #fffbeb; border: 1px solid #fcd34d; border-radius: 8px;
          padding: 15px 20px; margin-bottom: 20px; display: flex; align-items: center; gap: 15px;
          color: #b45309; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.1);
        }
      `}} />

      <button className="fabGlobalBtn" onClick={() => setShowGlobalModal(true)}>
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
        Vue Globale
      </button>

      {showGlobalModal && (
        <GlobalViewModal 
          onClose={() => setShowGlobalModal(false)} 
          globalRows={globalRows} 
        />
      )}

      <div className={styles.mainWrapper} style={{ minHeight: '100vh', height: 'auto', overflowY: 'visible', position: 'relative', zIndex: 1, paddingBottom: '100px' }}>
        <ShatteredGlass />
        <InteractiveBackground />

        <div className={`${styles.globalDragOverlay} ${isGlobalDragging ? styles.active : ''}`}>
          <div className={styles.globalDragContent}>
            <div className={styles.pulsingOrb}></div>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="17 8 12 3 7 8"></polyline>
              <line x1="12" y1="3" x2="12" y2="15"></line>
            </svg>
            <h2>Déposez le fichier spécifique pour {modules[activeModule]?.label}</h2>
          </div>
        </div>

        <div className={`${styles.uiTriggerZone} ${isMultiGroup && currentResult ? styles.expandedZone : ''}`} style={{ minHeight: '100vh', height: 'auto', paddingBottom: '2rem' }}>
          
          <div className={`${styles.glassContainer} ${isMultiGroup && currentResult ? styles.scrollableGlass : ''}`} style={{ maxHeight: 'none', height: 'auto', overflow: 'visible' }}>
            <div className={styles.header}>
              <div className={styles.logoWrapper}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                  <polyline points="2 17 12 22 22 17"></polyline>
                  <polyline points="2 12 12 17 22 12"></polyline>
                </svg>
              </div>
              <h1 className={styles.title}>Kyntus OS</h1>
              <p className={styles.subtitle}>Intelligence Qualité & Traitement de Données</p>
            </div>

            <div className={styles.moduleSelectorWrapper} style={{ display: 'block' }}>
              <div className="categorySection">
                <div className="categoryLabel">CATÉGORIE RACC</div>
                <div className={styles.moduleSelector} style={{ flexWrap: 'wrap', justifyContent: 'flex-start' }}>
                  {Object.values(modules).filter(mod => mod.category === 'RACC').map((mod) => (
                    <button key={mod.id} onClick={() => handleTabChange(mod.id)} className={`${styles.tabBtn} ${activeModule === mod.id ? styles.activeTab : ''}`}>
                      <span className={styles.tabIcon}>{mod.icon}</span> {mod.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="categorySection" style={{ marginBottom: 0 }}>
                <div className="categoryLabel" style={{ color: '#3b82f6' }}>CATÉGORIE SAV</div>
                <div className={styles.moduleSelector} style={{ flexWrap: 'wrap', justifyContent: 'flex-start' }}>
                  {Object.values(modules).filter(mod => mod.category === 'SAV').map((mod) => (
                    <button key={mod.id} onClick={() => handleTabChange(mod.id)} className={`${styles.tabBtn} ${activeModule === mod.id ? styles.activeTab : ''}`}>
                      <span className={styles.tabIcon}>{mod.icon}</span> {mod.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
            
            {error && (
              <div className={styles.errorAlert} style={{ marginTop: '20px' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <span>{error}</span>
              </div>
            )}

            <div className={styles.contentGrid}>
              
              {!currentResult && (
                <div className={styles.uploadSection}>
                  
                  {/* L'FIX HWA HNA: Zedt les sélecteurs dyal Mois w Année */}
                  {activeModule && (
                    <div className={styles.timeSelector}>
                      <div className={styles.timeGroup}>
                        <label>Mois de Traitement</label>
                        <select className={styles.timeSelect} value={selectedMonth} onChange={e => setSelectedMonth(e.target.value)}>
                          {[1,2,3,4,5,6,7,8,9,10,11,12].map(m => <option key={m} value={m}>{m}</option>)}
                        </select>
                      </div>
                      <div className={styles.timeGroup}>
                        <label>Année</label>
                        <select className={styles.timeSelect} value={selectedYear} onChange={e => setSelectedYear(e.target.value)}>
                          {[2024, 2025, 2026, 2027].map(y => <option key={y} value={y}>{y}</option>)}
                        </select>
                      </div>
                    </div>
                  )}

                  {showConfigButton && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '15px' }}>
                      <button onClick={() => setShowConfigPanel(!showConfigPanel)} className={`configBtn ${showConfigPanel ? 'active' : ''}`}>
                        <span style={{ width: '18px', display: 'flex' }}><IconSettings/></span>
                        {showConfigPanel ? 'Fermer les paramètres' : '⚙️ Paramétrer les Bonus'}
                      </button>
                    </div>
                  )}

                  {showConfigPanel && showConfigButton && (
                    <div className="configPanel">
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
                        <div style={{ padding: '8px', background: '#eff6ff', borderRadius: '8px', color: '#3b82f6' }}>
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                        </div>
                        <h4 style={{ margin: 0, color: '#0f172a', fontSize: '1.1rem' }}>
                          Ajustement Dynamique des Seuils (%) 
                          {(activeModule === 'GEM_NOK' || isFichier3Group) && <span style={{ color: '#ef4444', fontSize: '13px', marginLeft: '10px', padding: '4px 8px', background: '#fee2e2', borderRadius: '4px' }}>Logique Inverse Activée</span>}
                        </h4>
                      </div>
                      
                      {isFichier2Group && (
                        <table className="configTable">
                          <thead>
                            <tr>
                              <th style={{ textAlign: 'left' }}>Processus</th>
                              <th colSpan="2" style={{ color: '#3b82f6' }}>ZONE A</th>
                              <th colSpan="2" style={{ color: '#8b5cf6' }}>ZONE B</th>
                              <th colSpan="2" style={{ color: '#10b981' }}>ZONE C</th>
                            </tr>
                            <tr>
                              <th></th><th>MIN</th><th>MAX</th><th>MIN</th><th>MAX</th><th>MIN</th><th>MAX</th>
                            </tr>
                          </thead>
                          <tbody>
                            {['plp', 'hotline', 'construction', 'rang2'].map((process, idx) => (
                              <tr key={process} style={{ background: idx % 2 === 0 ? '#fff' : '#f8fafc', borderRadius: '8px' }}>
                                <td style={{ fontWeight: '800', textTransform: 'uppercase', color: '#334155', textAlign: 'left', paddingLeft: '15px' }}>{process === 'rang2' ? 'GLOBAL RANG 2' : process}</td>
                                {['a', 'b', 'c'].map(zone => (
                                  <React.Fragment key={`${process}-${zone}`}>
                                    <td><input type="number" step="0.1" className="configInput" value={bonusConfig[process][zone].min} onChange={(e) => handleConfigChange(process, zone, 'min', e.target.value)} /></td>
                                    <td><input type="number" step="0.1" className="configInput" value={bonusConfig[process][zone].max} onChange={(e) => handleConfigChange(process, zone, 'max', e.target.value)} /></td>
                                  </React.Fragment>
                                ))}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}

                      {isFichier3Group && (
                        <table className="configTable" style={{ width: '80%', margin: '0 auto' }}>
                          <thead>
                            <tr>
                              <th style={{ textAlign: 'left' }}>Indicateur</th>
                              <th style={{ color: '#ef4444' }}>Point MIN (L'khayb)</th>
                              <th style={{ color: '#10b981' }}>Point MAX (L'mzyan)</th>
                              <th style={{ color: '#F59E0B' }}>Bonus MAX</th>
                            </tr>
                          </thead>
                          <tbody>
                            {[{key: 'zmdAmii', label: 'ZMD AMII'}, {key: 'zmdRip', label: 'ZMD RIP'}, {key: 'ztd', label: 'ZTD'}].map((item, idx) => (
                              <tr key={item.key} style={{ background: idx % 2 === 0 ? '#fff' : '#f8fafc' }}>
                                <td style={{ fontWeight: '800', color: '#334155', textAlign: 'left', paddingLeft: '15px' }}>{item.label}</td>
                                <td><input type="number" step="0.1" className="configInput" value={bonusConfig[item.key].min} onChange={(e) => handleSingleConfigChange(item.key, 'min', e.target.value)} /></td>
                                <td><input type="number" step="0.1" className="configInput" value={bonusConfig[item.key].max} onChange={(e) => handleSingleConfigChange(item.key, 'max', e.target.value)} /></td>
                                <td><input type="number" step="0.1" className="configInput" style={{ borderColor: '#F59E0B', color: '#F59E0B' }} value={bonusConfig[item.key].bonusMax} onChange={(e) => handleSingleConfigChange(item.key, 'bonusMax', e.target.value)} /></td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}

                      {singleConfigKey && !isFichier3Group && (
                        <div style={{ display: 'flex', gap: '30px', justifyContent: 'center', background: '#f8fafc', padding: '20px', borderRadius: '12px' }}>
                          <div style={{ textAlign: 'center' }}>
                            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', color: activeModule === 'GEM_NOK' ? '#ef4444' : '#64748b', marginBottom: '8px' }}>
                              {activeModule === 'GEM_NOK' ? 'Point MIN (Lkhayb)' : 'Point MIN'}
                            </label>
                            <input type="number" step="0.1" className="configInput" value={bonusConfig[singleConfigKey].min} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'min', e.target.value)} />
                          </div>
                          <div style={{ textAlign: 'center' }}>
                            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', color: activeModule === 'GEM_NOK' ? '#10b981' : '#64748b', marginBottom: '8px' }}>
                              {activeModule === 'GEM_NOK' ? 'Point MAX (Lmzyan)' : 'Point MAX'}
                            </label>
                            <input type="number" step="0.1" className="configInput" value={bonusConfig[singleConfigKey].max} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'max', e.target.value)} />
                          </div>
                          <div style={{ textAlign: 'center' }}>
                            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', color: '#F59E0B', marginBottom: '8px' }}>Bonus MAX</label>
                            <input type="number" step="0.1" className="configInput" style={{ borderColor: '#F59E0B', color: '#F59E0B' }} value={bonusConfig[singleConfigKey].bonusMax} onChange={(e) => handleSingleConfigChange(singleConfigKey, 'bonusMax', e.target.value)} />
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  {!activeModule ? (
                    <div className={styles.placeholderMessage} style={{ marginTop: '20px' }}>
                      <svg className={styles.placeholderIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                        <path d="M15.05 5A5 5 0 0 1 19 8.95M15.05 1A9 9 0 0 1 23 8.94m-1 7.98v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                      </svg>
                      Sélectionnez un traitement ci-dessus<br/>pour déverrouiller la zone d'injection.
                    </div>
                  ) : !fileToUpload ? (
                    <div className={styles.dropZoneWrapper}>
                      <div 
                        className={`${styles.dropZone} ${isDragging ? styles.dragging : ''}`}
                        onDragOver={handleDragOver} onDragLeave={handleDragLeave} onDrop={handleDrop}
                        onClick={() => fileInputRef.current?.click()}
                      >
                        <input type="file" accept=".csv, .xlsx, .xls" onChange={handleFileChange} ref={fileInputRef} className={styles.hiddenInput} />
                        <div className={styles.dropZoneContent}>
                          <div className={styles.uploadIconWrapper}>
                            <div className={styles.iconPulse}></div>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                              <polyline points="17 8 12 3 7 8"></polyline>
                              <line x1="12" y1="3" x2="12" y2="15"></line>
                            </svg>
                          </div>
                          <h3>Injecter le fichier spécifique ({modules[activeModule]?.label})</h3>
                          <p>Formats supportés : CSV, XLSX, XLS</p>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className={styles.fileSelectedWrapper}>
                      <div className={styles.fileSelectedCard}>
                        <div className={styles.fileInfo}>
                          <div className={styles.fileIcon}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                          </div>
                          <div className={styles.fileDetails}>
                            <span className={styles.fileName}>{fileToUpload.name}</span>
                            <span className={styles.fileSize}>{(fileToUpload.size / 1024).toFixed(2)} KB</span>
                          </div>
                        </div>
                        <button onClick={removeFileToUpload} className={styles.removeBtn}>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </button>
                      </div>

                      <div className={styles.actionsContainer}>
                        <div className={styles.btnWrapper} style={{ width: '100%' }}>
                          <button onClick={handleAnalyze} disabled={loading} className={`${styles.actionBtn} ${styles.btnPrimary}`}>
                            <div className={styles.btnShine}></div>
                            {loading ? <span className={styles.spinner}></span> : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>}
                            <span>Lancer l'analyse Globale</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {currentResult && uiData && (
                <div className={styles.resultSection}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h3 className={styles.resultTitle}>{currentResult.title}</h3>
                    <button onClick={handleResetData} style={{ background: 'none', border: 'none', color: '#ff4b4b', cursor: 'pointer', fontSize: '13px', fontWeight: 'bold', textDecoration: 'underline' }}>
                      Réinitialiser ({isFichier2Group ? "Fichier 2 (Tous)" : isFichier3Group ? "Fichier 3 (Tous)" : modules[activeModule]?.label})
                    </button>
                  </div>

                  {/* L'FIX HWA HNA: Zedt l'Dropdown dyal l'Département */}
                  {currentResult.departments && currentResult.departments.length > 0 && (
                    <div className={styles.deptSelector}>
                      <label>Filtrer par Département :</label>
                      <select className={styles.deptSelect} value={selectedDept} onChange={e => setSelectedDept(e.target.value)}>
                        {currentResult.departments.map(d => (
                          <option key={d} value={d}>{d === "GLOBAL" ? "Vue Globale (Tous les départements)" : `Département ${d}`}</option>
                        ))}
                      </select>
                    </div>
                  )}
                  
                  {isFichier3Group && !uiData.isComboUnlocked && (
                    <div className="lockBannerLight">
                       <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                       <div>
                          <h4 style={{ margin: '0 0 4px 0', fontSize: '15px', fontWeight: '800' }}>Calcul en attente</h4>
                          <p style={{ margin: 0, fontSize: '13px' }}>
                            Veuillez injecter les 3 fichiers isolés (ZMD AMII, ZMD RIP, ZTD) pour débloquer la Part de Marché et le Bonus.
                          </p>
                       </div>
                    </div>
                  )}

                  {isMultiGroup ? (
                    <div className={styles.multiGroupResult}>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe A ({getZoneLabel('A')})</h4>
                        <div className={isBonusActive ? "grid5" : "grid4"}>
                          <ResultCard delay="0s" label="NUM" value={uiData.groupA.num} icon={<IconNum/>} />
                          <ResultCard delay="0.1s" label="DENUM" value={uiData.groupA.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.15s" label="Part (Poids)" value={`${uiData.groupA.partDeMarche || 0}%`} icon={<IconPie/>} />
                          <ResultCard delay="0.2s" highlight={true} label="Taux" value={`${uiData.groupA.resultat}%`} icon={<IconResult/>} />
                          {isBonusActive && <ResultCard delay="0.25s" highlight={true} label="Bonus Gagné" value={`+${uiData.groupA.bonus || 0}%`} icon={<IconBonus/>} />}
                        </div>
                      </div>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe B ({getZoneLabel('B')})</h4>
                        <div className={isBonusActive ? "grid5" : "grid4"}>
                          <ResultCard delay="0.1s" label="NUM" value={uiData.groupB.num} icon={<IconNum/>} />
                          <ResultCard delay="0.2s" label="DENUM" value={uiData.groupB.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.25s" label="Part (Poids)" value={`${uiData.groupB.partDeMarche || 0}%`} icon={<IconPie/>} />
                          <ResultCard delay="0.3s" highlight={true} label="Taux" value={`${uiData.groupB.resultat}%`} icon={<IconResult/>} />
                          {isBonusActive && <ResultCard delay="0.35s" highlight={true} label="Bonus Gagné" value={`+${uiData.groupB.bonus || 0}%`} icon={<IconBonus/>} />}
                        </div>
                      </div>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe C ({getZoneLabel('C')})</h4>
                        <div className={isBonusActive ? "grid5" : "grid4"}>
                          <ResultCard delay="0.2s" label="NUM" value={uiData.groupC.num} icon={<IconNum/>} />
                          <ResultCard delay="0.3s" label="DENUM" value={uiData.groupC.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.35s" label="Part (Poids)" value={`${uiData.groupC.partDeMarche || 0}%`} icon={<IconPie/>} />
                          <ResultCard delay="0.4s" highlight={true} label="Taux" value={`${uiData.groupC.resultat}%`} icon={<IconResult/>} />
                          {isBonusActive && <ResultCard delay="0.45s" highlight={true} label="Bonus Gagné" value={`+${uiData.groupC.bonus || 0}%`} icon={<IconBonus/>} />}
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className={singleGridClass}>
                      <ResultCard delay="0s" label="Numérateur (NUM)" value={uiData.num} icon={<IconNum/>} />
                      <ResultCard delay="0.1s" label="Dénominateur (DENUM)" value={uiData.denum} icon={<IconDenum/>} />
                      {isFichier3Group && <ResultCard delay="0.15s" label="Part (Poids)" value={pdmValueDisplay} icon={<IconPie/>} />}
                      <ResultCard delay="0.2s" highlight={true} label="Taux de réussite" value={`${uiData.resultat}%`} icon={<IconResult/>} />
                      {isBonusActive && <ResultCard delay="0.3s" highlight={true} label="Bonus Gagné" value={bonusValueDisplay} icon={<IconBonus/>} />}
                    </div>
                  )}

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