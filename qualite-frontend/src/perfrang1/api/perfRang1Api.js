export const uploadPerfRang1Excel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(process.env.NEXT_PUBLIC_API_URL + " /api/v1/excel/perfrang1/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier PERF RANG 1");
  }

  return response.json();
};