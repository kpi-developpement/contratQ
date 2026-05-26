# Beddelna Node 18 b Node 20 bach y-khdem m3a Next.js
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Kan-récupériw l'URL d l'API mn l'docker-compose (li jaya mn Jenkins)
ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# N'forciw Next.js y-lancer f l'port jdid
ENV PORT=8741

RUN npm run build

EXPOSE 8741

CMD ["npm", "start"]