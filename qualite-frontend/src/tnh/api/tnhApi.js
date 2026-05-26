export const uploadTnhExcel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(process.env.NEXT_PUBLIC_API_URL + " /api/v1/excel/tnh/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier TNH");
  }

  return response.json();
};