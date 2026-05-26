"use client";

import { useState, useRef, useEffect } from "react";
import styles from "../styles/card.module.css";

export default function ResultCard({ label, value, icon, delay = "0s", highlight = false }) {
  const cardRef = useRef(null);
  const [position, setPosition] = useState({ x: 50, y: 50 });
  const [isHovered, setIsHovered] = useState(false);

  const handleMouseMove = (e) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Calculs l 3D Rotation w l'Glow
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    const rotateX = ((y - centerY) / centerY) * -15;
    const rotateY = ((x - centerX) / centerX) * 15;

    setPosition({ x: (x / rect.width) * 100, y: (y / rect.height) * 100 });
    
    cardRef.current.style.setProperty('--rx', `${rotateX}deg`);
    cardRef.current.style.setProperty('--ry', `${rotateY}deg`);
    cardRef.current.style.setProperty('--mx', `${x}px`);
    cardRef.current.style.setProperty('--my', `${y}px`);
  };

  const handleMouseEnter = () => setIsHovered(true);

  const handleMouseLeave = () => {
    setIsHovered(false);
    if (cardRef.current) {
      cardRef.current.style.setProperty('--rx', `0deg`);
      cardRef.current.style.setProperty('--ry', `0deg`);
      setPosition({ x: 50, y: 50 });
    }
  };

  return (
    <div className={styles.cardWrapper} style={{ animationDelay: delay }}>
      <div 
        ref={cardRef}
        className={`${styles.card3D} ${highlight ? styles.highlightCard : ''} ${isHovered ? styles.hovered : ''}`}
        onMouseMove={handleMouseMove}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        {/* L'Glow li kaytbe3 l'souris */}
        <div className={styles.magneticGlow}></div>
        
        {/* Les Bordures li kaylme3o */}
        <div className={styles.borderGlow}></div>

        <div className={styles.cardBackground}></div>
        
        <div className={styles.contentLayer}>
          <div className={styles.iconWrapper}>
            {icon}
          </div>
          <div className={styles.textWrapper}>
            <span className={styles.label}>{label}</span>
            <span className={styles.value}>{value}</span>
          </div>
        </div>
      </div>
    </div>
  );
}