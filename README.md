# springboot-app

Sample Spring Boot app for the on-prem HA Kubernetes CI/CD + GitOps project.

Pipeline (Jenkinsfile): Maven build & test → SonarQube + Quality Gate → Docker build →
Trivy scan → push `sorna724/springboot-app` to Docker Hub → update image tag in
`springboot-gitops` → Argo CD deploys.
