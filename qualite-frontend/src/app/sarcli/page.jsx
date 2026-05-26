import SarcliUploader from "@/sarclinok/components/SarcliUploader";

export const metadata = {
  title: 'SARCLI NOK | Qualité',
  description: 'Analyse des données SARCLI NOK',
};

export default function SarcliPage() {
  return (
    <main>
      <SarcliUploader />
    </main>
  );
}