export const uploadPerfRang2Excel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("http://localhost:7623/api/v1/excel/perfrang2/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier PERF RANG 2");
  }

  return response.json();
};