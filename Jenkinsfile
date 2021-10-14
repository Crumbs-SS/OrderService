pipeline{

     agent any

  environment
  {
          IMG_NAME = "orderservice"
          AWS_ID = credentials('AWS_ID')
          DB_ENDPOINT = credentials('DB_ENDPOINT')
          DB_USERNAME = credentials('DB_USERNAME')
          DB_PASSWORD = credentials('DB_PASSWORD')
          AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
          AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
          JWT_SECRET = credentials('JWT_SECRET')
          GMAPS_API_KEY = credentials('prod/crumbs/geo')
  }
  tools
  {
            maven 'maven'
            jdk 'java'
  }


  stages
  {
       stage("Build")
       {
            steps {
                 sh 'mvn clean install'
            }
       }
       
       stage("Test")
       {
                steps
                {
                    sh 'mvn test'
                    junit '**/target/surefire-reports/*.xml'
                }
       } 
         
       stage('Code Analysis: Sonarqube')
       {
                   steps {
                       withSonarQubeEnv('sonarqube') {
                           sh 'mvn sonar:sonar'
                       }
                   }
       }
       stage('Await Quality Gateway') 
       {
            steps {
                waitForQualityGate abortPipeline: false
            }
       }
       
      stage("Package")
      {
            steps
            {
                sh 'mvn clean package'
            }
      }
      stage("Docker Build") {

          steps {
              echo "Docker Build...."
              withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'jenkins_credentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        sh "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${AWS_ID}.dkr.ecr.us-east-1.amazonaws.com"
              }
              sh "docker build -t ${IMG_NAME} ."
               sh "docker tag ${IMG_NAME}:latest ${AWS_ID}.dkr.ecr.us-east-1.amazonaws.com/${IMG_NAME}:latest"
              echo "Docker Push..."
               sh "docker push ${AWS_ID}.dkr.ecr.us-east-1.amazonaws.com/${IMG_NAME}:latest"
          }
      }
    }
  post
  {
          always
          {
              sh 'mvn clean'
              sh "docker rmi \$(docker images --format \'{{.Repository}}:{{.Tag}}\' | grep \'${IMG_NAME}\')"
          }
  }

}
