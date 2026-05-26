export const uploadSacliExcel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  // Bdelna l'port hna l 7623
  const response = await fetch(process.env.NEXT_PUBLIC_API_URL + " /api/v1/excel/sacli/analyze", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Erreur f l'analyse dyal l'fichier");
  }

  return response.json();
};