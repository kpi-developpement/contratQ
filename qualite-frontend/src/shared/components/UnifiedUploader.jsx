"use client";

import { useState, useRef, useEffect } from "react";
import InteractiveBackground from "../threejs/InteractiveBackground";
import ShatteredGlass from "./ShatteredGlass";
import ResultCard from "./ResultCard";
import MasterTable from "./MasterTable";
import styles from "../styles/unified.module.css";

// Endpoints dyal les fichiers 1 (Mfer9in)
const API_ENDPOINTS_FICHIER_1 = {
  SACLI_OK: "/api/v1/excel/sacli/analyze",
  SARCLI_NOK: "/api/v1/excel/sarcli/analyze",
};

export default function UnifiedUploader() {
  const [activeModule, setActiveModule] = useState(null); 
  
  // ==========================================
  // STATES DYAL LES CACHES (L'Intelligence Hna)
  // ==========================================
  const [fichier1Cache, setFichier1Cache] = useState({}); // SACLI w SARCLI kol wa7d bo7do
  const [fichier2Data, setFichier2Data] = useState(null); // Jame3 TNH w RANG1 w RANG2 kamlin!
  const [fichier3Data, setFichier3Data] = useState(null); // Jame3 ZMD AMII, ZMD RIP, w ZTD!
  
  const [fileToUpload, setFileToUpload] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isGlobalDragging, setIsGlobalDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const fileInputRef = useRef(null);

  const modules = {
    SACLI_OK: { id: 'SACLI_OK', label: 'SACLI OK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"></path><path d="m9 12 2 2 4-4"></path></svg> },
    SARCLI_NOK: { id: 'SARCLI_NOK', label: 'SARCLI NOK', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg> },
    TNH: { id: 'TNH', label: 'TNH', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
    PERF_RANG_1: { id: 'PERF_RANG_1', label: 'RANG 1 (PLP)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg> },
    HOTLINE_RANG_1: { id: 'HOTLINE_RANG_1', label: 'RANG 1 (HOTLINE)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg> },
    CONSTRUCTION_RANG_1: { id: 'CONSTRUCTION_RANG_1', label: 'RANG 1 (CONSTRUCT)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path></svg> },
    PERF_RANG_2: { id: 'PERF_RANG_2', label: 'PERF RANG 2 (TOUT)', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M7 13l5 5 5-5M7 6l5 5 5-5"/></svg> },
    ZMD_AMII: { id: 'ZMD_AMII', label: 'ZMD AMII', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"></path></svg> },
    ZMD_RIP: { id: 'ZMD_RIP', label: 'ZMD RIP', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path></svg> },
    ZTD: { id: 'ZTD', label: 'ZTD', icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"><rect x="4" y="2" width="16" height="20" rx="2" ry="2"></rect><path d="M9 22v-4h6v4"></path><path d="M8 6h.01"></path><path d="M16 6h.01"></path><path d="M12 6h.01"></path><path d="M12 10h.01"></path><path d="M12 14h.01"></path><path d="M16 10h.01"></path><path d="M16 14h.01"></path><path d="M8 10h.01"></path><path d="M8 14h.01"></path></svg> }
  };

  const isFichier1Group = ["SACLI_OK", "SARCLI_NOK"].includes(activeModule);
  const isFichier3Group = ["ZMD_AMII", "ZMD_RIP", "ZTD"].includes(activeModule);
  const isFichier2Group = !isFichier1Group && !isFichier3Group;
  
  const isMultiGroup = ["PERF_RANG_1", "HOTLINE_RANG_1", "CONSTRUCTION_RANG_1", "PERF_RANG_2"].includes(activeModule);

  // ==========================================
  // EXTRACTION DES DONNEES (L'Backend MAPPING)
  // ==========================================
  const getCurrentResult = () => {
    if (!activeModule) return null;
    
    // Fichier 1 (Mfer9in)
    if (isFichier1Group && fichier1Cache[activeModule]) {
      return fichier1Cache[activeModule];
    } 
    // Fichier 3 (Mjemou3)
    else if (isFichier3Group && fichier3Data) {
      if (activeModule === "ZMD_AMII" && fichier3Data.zmdAmii) return { title: "Rapport : ZMD AMII", data: fichier3Data.zmdAmii };
      if (activeModule === "ZMD_RIP" && fichier3Data.zmdRip) return { title: "Rapport : ZMD RIP", data: fichier3Data.zmdRip };
      if (activeModule === "ZTD" && fichier3Data.ztd) return { title: "Rapport : ZTD", data: fichier3Data.ztd };
    }
    // Fichier 2 (Mjemou3) -> Kima f l'DTO dyal Backend b dbt!
    else if (isFichier2Group && fichier2Data) {
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
    setActiveModule(moduleId);
    setFileToUpload(null); 
    setError("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  useEffect(() => {
    const handleGlobalDragOver = (e) => {
      e.preventDefault();
      if (!fileToUpload && activeModule && !currentResult) setIsGlobalDragging(true);
    };
    const handleGlobalDragLeave = (e) => {
      e.preventDefault();
      if (e.clientX === 0 && e.clientY === 0) setIsGlobalDragging(false);
    };
    const handleGlobalDrop = (e) => {
      e.preventDefault();
      setIsGlobalDragging(false);
    };

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
    e.preventDefault(); 
    setIsDragging(false);
    setIsGlobalDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFileToUpload(e.dataTransfer.files[0]);
      setError("");
    }
  };
  
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFileToUpload(e.target.files[0]);
      setError("");
    }
  };

  const removeFileToUpload = () => {
    setFileToUpload(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleResetData = () => {
    if (isFichier1Group) {
      setFichier1Cache(prev => { const newC = {...prev}; delete newC[activeModule]; return newC; });
    } else if (isFichier3Group) {
      setFichier3Data(null); 
    } else {
      setFichier2Data(null); 
    }
    setFileToUpload(null);
  };

  const handleAnalyze = async () => {
    if (!fileToUpload || !activeModule) { 
      setError("Veuillez injecter un fichier d'abord."); 
      return; 
    }
    
    setLoading(true); 
    setError(""); 
    
    // Déterminer l'Endpoint 3la 7ssab l'Group dyal l'Fichier
    let endpoint = "";
    if (isFichier1Group) endpoint = API_ENDPOINTS_FICHIER_1[activeModule];
    else if (isFichier3Group) endpoint = "/api/v1/dashboard/fichier3";
    else endpoint = "/api/v1/dashboard/fichier2";

    const formData = new FormData();
    formData.append('file', fileToUpload);

    try {
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7623';
      const res = await fetch(`${baseUrl}${endpoint}`, { 
        method: 'POST', 
        body: formData 
      });
      
      if (!res.ok) throw new Error("Erreur serveur");
      const fullData = await res.json();
      
      // Stocker l'data f l'cache l'mounasib
      if (isFichier1Group) {
        const title = `Rapport : ${modules[activeModule].label}`;
        setFichier1Cache(prev => ({ ...prev, [activeModule]: { title, data: fullData } }));
      } 
      else if (isFichier3Group) {
        setFichier3Data(fullData);
      } 
      else {
        setFichier2Data(fullData);
      }
      
      setFileToUpload(null); 
    } catch (err) {
      console.error("Fetch Error: ", err);
      setError(`Échec de l'analyse. Vérifiez l'Backend w l'URL.`);
    } finally {
      setLoading(false);
    }
  };

  const IconNum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>;
  const IconDenum = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>;
  const IconResult = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>;

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
      groupA: raw.groupA || { num: 0, denum: 0, resultat: 0 },
      groupB: raw.groupB || { num: 0, denum: 0, resultat: 0 },
      groupC: raw.groupC || { num: 0, denum: 0, resultat: 0 },
    };
  };

  const uiData = formatDataForUI();

  return (
    <>
      <style dangerouslySetInnerHTML={{__html: `
        body, html { background-color: #ffffff !important; margin: 0; padding: 0; overflow-x: hidden; overflow-y: auto !important; }
        canvas { position: fixed !important; top: 0; left: 0; z-index: -1; }
      `}} />

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

            <div className={styles.moduleSelectorWrapper}>
              <div className={styles.moduleSelector}>
                {Object.values(modules).map((mod) => (
                  <button 
                    key={mod.id}
                    onClick={() => handleTabChange(mod.id)}
                    className={`${styles.tabBtn} ${activeModule === mod.id ? styles.activeTab : ''}`}
                  >
                    <span className={styles.tabIcon}>{mod.icon}</span> 
                    {mod.label}
                  </button>
                ))}
              </div>
            </div>
            
            {error && (
              <div className={styles.errorAlert}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <span>{error}</span>
              </div>
            )}

            <div className={styles.contentGrid}>
              
              {!currentResult && (
                <div className={styles.uploadSection}>
                  {!activeModule ? (
                    <div className={styles.placeholderMessage}>
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
                            <span>Lancer l'analyse Globale ({isFichier2Group ? 'Fichier 2' : isFichier3Group ? 'Fichier 3' : modules[activeModule]?.label})</span>
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
                  
                  {isMultiGroup ? (
                    <div className={styles.multiGroupResult}>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe A ({getZoneLabel('A')})</h4>
                        <div className={styles.resultGridSmall}>
                          <ResultCard delay="0s" label="NUM" value={uiData.groupA.num} icon={<IconNum/>} />
                          <ResultCard delay="0.1s" label="DENUM" value={uiData.groupA.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.2s" highlight={true} label="Taux" value={`${uiData.groupA.resultat}%`} icon={<IconResult/>} />
                        </div>
                      </div>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe B ({getZoneLabel('B')})</h4>
                        <div className={styles.resultGridSmall}>
                          <ResultCard delay="0.1s" label="NUM" value={uiData.groupB.num} icon={<IconNum/>} />
                          <ResultCard delay="0.2s" label="DENUM" value={uiData.groupB.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.3s" highlight={true} label="Taux" value={`${uiData.groupB.resultat}%`} icon={<IconResult/>} />
                        </div>
                      </div>
                      <div className={styles.groupContainer}>
                        <h4 className={styles.groupTitle}>Groupe C ({getZoneLabel('C')})</h4>
                        <div className={styles.resultGridSmall}>
                          <ResultCard delay="0.2s" label="NUM" value={uiData.groupC.num} icon={<IconNum/>} />
                          <ResultCard delay="0.3s" label="DENUM" value={uiData.groupC.denum} icon={<IconDenum/>} />
                          <ResultCard delay="0.4s" highlight={true} label="Taux" value={`${uiData.groupC.resultat}%`} icon={<IconResult/>} />
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className={styles.resultGrid}>
                      <ResultCard delay="0s" label="Numérateur (NUM)" value={uiData.num} icon={<IconNum/>} />
                      <ResultCard delay="0.1s" label="Dénominateur (DENUM)" value={uiData.denum} icon={<IconDenum/>} />
                      <ResultCard delay="0.2s" highlight={true} label="Taux de réussite" value={`${uiData.resultat}%`} icon={<IconResult/>} />
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