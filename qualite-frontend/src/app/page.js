// src/app/page.js
import UnifiedUploader from "@/shared/components/UnifiedUploader";
import styles from "./page.module.css";

export const metadata = {
  title: 'Dashboard Qualité | Kyntus OS',
  description: 'Plateforme de traitement des fichiers qualité',
};

export default function Home() {
  return (
    <main className={styles.mainWrapper}>
      {/* Background animé */}
      <div className={styles.glow1}></div>
      <div className={styles.glow2}></div>
      <div className={styles.glow3}></div>
      
      {/* Contenu principal */}
      <div className={styles.content}>
        <UnifiedUploader />
      </div>
    </main>
  );
}