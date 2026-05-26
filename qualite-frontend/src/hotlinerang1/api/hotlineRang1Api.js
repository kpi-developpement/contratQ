export const uploadHotlineRang1Excel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("http://localhost:7623/api/v1/excel/hotlinerang1/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier HOTLINE RANG 1");
  }

  return response.json();
};