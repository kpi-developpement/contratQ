export const uploadGemNokExcel = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7623";
  const response = await fetch(`${baseUrl}/api/v1/excel/gemnok/analyze`, { method: "POST", body: formData });
  if (!response.ok) throw new Error("Erreur f l'analyse dyal GEM NOK");
  return response.json();
};