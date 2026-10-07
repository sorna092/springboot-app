// CI pipeline: Maven build -> SonarQube + Quality Gate -> Docker build
// -> Trivy scan -> push to Docker Hub -> bump image tag in GitOps repo (Argo CD deploys)
pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  // Jenkins is on a private IP, so GitHub webhooks can't reach it -> poll every 2 min.
  // (Use a webhook instead if you expose Jenkins via a tunnel.)
  triggers { pollSCM('H/2 * * * *') }

  environment {
    IMAGE       = 'sorna724/springboot-app'
    GITOPS_REPO = 'github.com/sorna092/springboot-gitops.git'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.TAG = "${env.BUILD_NUMBER}-${sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()}"
        }
        echo "Image tag: ${env.TAG}"
      }
    }

    stage('Maven Build & Test') {
      steps { sh 'mvn -B clean verify' }
      post { always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' } }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('sonarqube') {
          sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Docker Build') {
      steps { sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .' }
    }

    stage('Trivy Image Scan') {
      steps {
        sh '''
          trivy image --no-progress --ignore-unfixed \
            --severity HIGH,CRITICAL --exit-code 1 $IMAGE:$TAG
        '''
      }
    }

    stage('Push to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                          usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh '''
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
            docker push $IMAGE:$TAG
            docker push $IMAGE:latest
            docker logout
          '''
        }
      }
    }

    stage('Update GitOps Repo') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'github-creds',
                                          usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
          sh '''
            rm -rf gitops
            git clone "https://${GH_USER}:${GH_TOKEN}@${GITOPS_REPO}" gitops
            cd gitops
            sed -i "s|image: .*springboot-app:.*|image: ${IMAGE}:${TAG}|" k8s/deployment.yaml
            sed -i "s|value: \\".*\\" # APP_VERSION|value: \\"${TAG}\\" # APP_VERSION|" k8s/deployment.yaml
            git config user.email "jenkins@cicd.lab.local"
            git config user.name  "Jenkins CI"
            git add k8s/deployment.yaml
            git diff --cached --quiet || git commit -m "ci: deploy ${IMAGE}:${TAG}"
            git push origin main
          '''
        }
      }
    }
  }

  post {
    always {
      sh 'docker rmi $IMAGE:$TAG $IMAGE:latest 2>/dev/null || true'
    }
  }
}
