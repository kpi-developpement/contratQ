export const uploadSarcliExcel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(process.env.NEXT_PUBLIC_API_URL + " /api/v1/excel/sarcli/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier SARCLI");
  }

  return response.json();
};