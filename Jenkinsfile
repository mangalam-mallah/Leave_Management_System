pipeline {
    agent any

    environment {
        DB_NAME = credentials('DB_NAME')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        DB_ROOT_PASSWORD = credentials('DB_ROOT_PASSWORD')

        JWT_SECRET_KEY = credentials('JWT_SECRET_KEY')
        JWT_EXPIRATION = credentials('JWT_EXPIRATION')

        MAIL_USERNAME = credentials('MAIL_USERNAME')
        MAIL_PASSWORD = credentials('MAIL_PASSWORD')
    }

    stages {

        stage('Build App') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Run Containers') {
            steps {
                sh '''
                docker compose down -v
                docker compose up -d --build
                '''
            }
        }
    }
}