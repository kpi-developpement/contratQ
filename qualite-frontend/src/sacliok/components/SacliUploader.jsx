"use client";

import { useState } from "react";
import { uploadSacliExcel } from "../api/sacliApi";
import styles from "../styles/sacli.module.css";

export default function SacliUploader() {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setError("");
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError("Khassk t3zel fichier Excel b3da!");
      return;
    }

    setLoading(true);
    setError("");
    
    try {
      const data = await uploadSacliExcel(file);
      setResult(data);
    } catch (err) {
      setError("W9e3 mouchkil f l'analyse dyal l'fichier. T2ked mn serveur Spring Boot.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Traitement SACLI OK</h2>
      
      {error && <div className={styles.error}>{error}</div>}

      <div className={styles.uploadSection}>
        <input 
          type="file" 
          accept=".xlsx, .xls" 
          onChange={handleFileChange}
          className={styles.fileInput}
        />
        <button 
          onClick={handleUpload} 
          disabled={!file || loading}
          className={styles.submitBtn}
        >
          {loading ? "Jari l'analyse..." : "Lancer l'Analyse"}
        </button>
      </div>

      {result && (
        <div className={styles.resultCards}>
          <div className={`${styles.card} ${styles.cardNum}`}>
            <span className={styles.cardLabel}>NUM (Valeur = 5)</span>
            <span className={styles.cardValue}>{result.num}</span>
          </div>
          
          <div className={`${styles.card} ${styles.cardDenum}`}>
            <span className={styles.cardLabel}>DENUM (B. TOT)</span>
            <span className={styles.cardValue}>{result.denum}</span>
          </div>
          
          <div className={`${styles.card} ${styles.cardResult}`}>
            <span className={styles.cardLabel}>Résultat</span>
            <span className={`${styles.cardValue} ${styles.resultHighlight}`}>
              {result.resultat}%
            </span>
          </div>
        </div>
      )}
    </div>
  );
}