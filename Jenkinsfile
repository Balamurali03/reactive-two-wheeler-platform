pipeline {
    agent any

    environment {
        DOCKER_BUILDKIT = '1'
        COMPOSE_DOCKER_CLI_BUILD = '1'
    }

    stages {

        stage('Checkout Code') {
            steps {
                echo '📥 Cloning repository...'
                checkout scm
            }
        }

        stage('Build JARs (Maven)') {
            steps {
                echo '☕ Building Spring Boot services...'
                sh '''
                  set -e

                  echo "Building auth-service"
                  cd auth-service
                  mvn clean package -DskipTests
                  cd ..

                  echo "Building user-service"
                  cd user-service
                  mvn clean package -DskipTests
                  cd ..

                  # Future services (uncomment when added)
                  # echo "Building vehicle-service"
                  # cd vehicle-service
                  # mvn clean package -DskipTests
                  # cd ..
                '''
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker images...'
                sh '''
                  docker compose build
                '''
            }
        }

        stage('Deploy Services') {
            steps {
                echo '🚀 Deploying services with Docker Compose...'
                sh '''
                  docker compose up -d
                '''
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed. Check logs.'
        }
        always {
            echo '📦 Pipeline execution finished.'
        }
    }
}
