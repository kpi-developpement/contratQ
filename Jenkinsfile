pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "contratq-prod"
    }

    stages {
        stage('🧹 Clean & Checkout') {
            steps {
                script {
                    echo "=> [ÉTAPE 1] Récupération d l'code jdid mn GitHub..."
                    cleanWs()
                    checkout scm
                }
            }
        }

        stage('💥 Frappe Chirurgicale (Risk Management)') {
            steps {
                script {
                    echo "=> [ÉTAPE 2] N-tiy7ou GHIR l-containers dyal ContratQ..."
                    // 🛡️ THE SHIELD: Kan-ms7ou ghir l'Back w l'Front. L'DB w Volume b3aaad w trankil!
                    sh "docker rm -f contratq_backend_prod contratq_frontend_prod || true"
                }
            }
        }

        stage('🚀 Build & Deploy (ContratQ)') {
            steps {
                script {
                    echo "=> [ÉTAPE 3] Lancement dyal l'ecosysteme ContratQ..."
                    // up -d --build kat-bni w kat-lanci bla ma t-msse7 volumes
                    sh "docker compose up -d --build"
                }
            }
        }

        stage('🛡️ Clean Up (Images)') {
            steps {
                script {
                    echo "=> [ÉTAPE 4] Nettoyage dyal les images l-qdam..."
                    sh "docker image prune -f"
                }
            }
        }
    }

    post {
        success {
            echo "========================================================"
            echo "✅ DÉPLOIEMENT CONTRATQ RÉUSSI !"
            echo "🌐 Frontend: http://10.10.10.25:8741"
            echo "⚙️ Backend: http://10.10.10.25:7623"
            echo "🛡️ BASE DE DONNÉES SÉCURISÉE (Volume Intact 100%)"
            echo "========================================================"
        }
        failure {
            echo "❌ ÉCHEC DU DÉPLOIEMENT."
        }
    }
}