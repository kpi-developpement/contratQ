"use client";

import styles from "../styles/unified.module.css";

export default function ShatteredGlass() {
  return (
    <div className={styles.shatteredGlassOverlay}>
      <svg viewBox="0 0 100 100" preserveAspectRatio="none" width="100%" height="100%">
        <defs>
          {/* Ddel w ddo bach ch9a9i ybano 3D w madyin (Wedde7nahom kter) */}
          <filter id="crack-shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0.3" dy="0.3" stdDeviation="0.4" floodColor="#0f172a" floodOpacity="0.4" />
            <feDropShadow dx="-0.2" dy="-0.2" stdDeviation="0.2" floodColor="#ffffff" floodOpacity="0.9" />
          </filter>
        </defs>

        {/* 
          0. L'West (T9ba) - Ghame9 ghir "zeee9a"
          Hada kay3ti dak l'effet dyal zaj r9i9 w ghame9 chwiya f l'west
        */}
        <path
          d="
            M 35 45 
            L 38 38 L 45 32 L 52 36 L 58 30 
            L 65 38 L 62 48 L 68 55 L 60 65 
            L 52 60 L 45 68 L 38 60 L 32 52 Z
          "
          fill="rgba(10, 17, 40, 0.06)" 
        />

        {/* 
          1. L'Zaj lkbir (Frosted Glass Body) 
          Kayghṭi l'ecran kaml w fih t9ba m3wja f l'west.
        */}
        <path
          className={styles.glassBody}
          fillRule="evenodd"
          d="
            M 0 0 L 100 0 L 100 100 L 0 100 Z 
            M 35 45 
            L 38 38 L 45 32 L 52 36 L 58 30 
            L 65 38 L 62 48 L 68 55 L 60 65 
            L 52 60 L 45 68 L 38 60 L 32 52 Z
          "
        />

        {/* 2. Jnab dyal T9ba (Sharp Edges) - Ghladin w madyin */}
        <path
          d="
            M 35 45 
            L 38 38 L 45 32 L 52 36 L 58 30 
            L 65 38 L 62 48 L 68 55 L 60 65 
            L 52 60 L 45 68 L 38 60 L 32 52 Z
          "
          fill="none"
          stroke="rgba(255, 255, 255, 0.95)"
          strokeWidth="0.4"
          filter="url(#crack-shadow)"
        />

        {/* 3. Ch9a9i Twal (Radiating Cracks) - Kttarnahom w wedde7nahom */}
        <g stroke="rgba(255, 255, 255, 0.85)" strokeWidth="0.25" fill="none" filter="url(#crack-shadow)">
          <path d="M 45 32 L 42 25 L 46 18 L 38 10 L 42 0 L 35 -10" />
          <path d="M 52 36 L 55 25 L 50 15 L 55 0" /> {/* Jdid */}
          <path d="M 58 30 L 62 22 L 58 15 L 65 5 L 60 -10" />
          <path d="M 65 38 L 75 35 L 82 28 L 90 32 L 100 20 L 110 15" />
          <path d="M 62 48 L 75 45 L 85 48 L 100 40" /> {/* Jdid */}
          <path d="M 68 55 L 78 58 L 85 52 L 95 60 L 110 55" />
          <path d="M 60 65 L 65 75 L 58 85 L 68 95 L 60 110" />
          <path d="M 52 60 L 55 75 L 50 90 L 55 110" /> {/* Jdid */}
          <path d="M 45 68 L 42 80 L 48 90 L 40 100 L 45 110" />
          <path d="M 38 60 L 28 65 L 22 58 L 12 65 L 0 60 L -10 65" />
          <path d="M 32 52 L 20 55 L 10 50 L 0 55" /> {/* Jdid */}
          <path d="M 35 45 L 25 42 L 18 35 L 8 38 L 0 25 L -10 20" />
        </g>

        {/* 4. Chbka dyal zaj (Spiderwebs) - Kttarnahom */}
        <g stroke="rgba(255, 255, 255, 0.6)" strokeWidth="0.15" fill="none" filter="url(#crack-shadow)">
          <path d="M 42 25 L 50 28 L 62 22 L 70 30 L 75 35 L 72 45 L 78 58 L 70 68 L 65 75 L 55 72 L 42 80 L 35 70 L 28 65 L 30 55 L 25 42 L 32 35 Z" />
          <path d="M 38 10 L 48 15 L 58 15 L 68 20 L 82 28 L 85 40 L 95 60 L 88 75 L 68 95 L 52 88 L 48 90 L 35 85 L 22 58 L 18 45 L 18 35 Z" />
          <path d="M 46 18 L 55 25 L 65 38" /> {/* Extra web */}
          <path d="M 22 58 L 20 55 L 25 42" /> {/* Extra web */}
          <path d="M 65 75 L 55 75 L 50 90" /> {/* Extra web */}
        </g>

        {/* 5. Trifat sghar kayti7o f l'west (Floating Shards) */}
        <polygon points="45,40 48,38 46,42" fill="rgba(255,255,255,0.8)" filter="url(#crack-shadow)" />
        <polygon points="55,45 58,43 56,48" fill="rgba(255,255,255,0.5)" filter="url(#crack-shadow)" />
        <polygon points="40,55 42,58 38,56" fill="rgba(255,255,255,0.9)" filter="url(#crack-shadow)" />
        <polygon points="50,50 52,48 49,52" fill="rgba(212,175,55,0.6)" filter="url(#crack-shadow)" /> {/* Gold shard */}
      </svg>
    </div>
  );
}