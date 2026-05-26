pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "contratq-prod"
        // 🔥 L'VARIABLE DYNAMIQUE: Hna t-7et l'IP wla l'Domaine dyal l'serveur dyalek
        API_URL = "http://YOUR_SERVER_IP:7623"
    }

    stages {
        stage('🧹 Clean & Checkout') {
            steps {
                script {
                    echo "=> [ÉTAPE 1] Nettoyage w téléchargement dyal l'Code ContratQ mn GitHub..."
                    cleanWs()
                    checkout scm
                }
            }
        }

        stage('🛑 Teiyya7 l-9dim (Free Ports)') {
            steps {
                script {
                    echo "=> [ÉTAPE 2] Arrêt dyal l'ancienne version bach n-khewiw les ports..."
                    sh "docker compose -f docker-compose.yml down || true"
                }
            }
        }

        stage('🚀 Build & Deploy Jdid') {
            steps {
                script {
                    echo "=> [ÉTAPE 3] Lancement dyal l'architecture jdida..."
                    // Kan-passiw API_URL l'docker-compose bach y-injectiha f l'build dyal Next.js
                    sh "API_URL=${API_URL} docker compose -f docker-compose.yml up -d --build"
                }
            }
        }

        stage('🛡️ Risk Management (Clean Up)') {
            steps {
                script {
                    echo "=> [ÉTAPE 4] Nettoyage dyal les vieilles images w les réseaux orphelins..."
                    sh "docker system prune -af --volumes"
                }
            }
        }
    }

    post {
        success {
            echo "✅ DÉPLOIEMENT CONTRATQ RÉUSSI ABRO!"
            echo "L'API khdama f port 7623, w l'Frontend f port 8741."
        }
        failure {
            echo "❌ MOCHKIL F L'DEPLOIEMENT ! Checki l'logs dyal Jenkins."
        }
    }
}