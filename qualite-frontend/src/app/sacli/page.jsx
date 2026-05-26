import SacliUploader from "@/sacliok/components/SacliUploader";

export const metadata = {
  title: 'SACLI OK | Qualité',
  description: 'Analyse des données SACLI OK',
};

export default function SacliPage() {
  return (
    <main>
      <SacliUploader />
    </main>
  );
}
