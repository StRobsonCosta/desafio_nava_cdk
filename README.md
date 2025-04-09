# Deploy da Aplicação de Logística na AWS com CDK

Este guia contém os passos necessários para fazer o deploy da sua aplicação de logística desenvolvida em Java 17 com Spring Boot na AWS usando AWS CDK.

---

## 📌 **Pré-requisitos**

1. AWS CLI instalada e configurada (`aws configure`)
2. CDK instalado (`npm install -g aws-cdk`)
3. Docker instalado e rodando (para build da imagem)
4. Conta AWS com permissões necessárias

---

## 🚀 **Passo a passo do Deploy**

### 1️⃣  **Criar e Publicar a Imagem Docker**

1. **Gerar .jar em /target para build de Imagem:**

   ```sh
   mvn clean package
   ```
   ou caso queira ignorar o testes:

   ```sh
   mvn clean package -DskipTests
   ```

2. **Construir a imagem Docker:**

   ```sh
   docker build -t nava-log-app .
   ```

3. **(Se Necessário) Fazer login no DockerHub ou ECR:**

   ```sh
   docker login
   ```

4. **Enviar a imagem para o DockerHub (ou ECR caso use AWS):**

   ```sh
   docker tag nava-log-app:latest meu-usuario/nava-log-app:latest
   docker push meu-usuario/nava-log-app:latest
   ```

Se estiver usando Amazon ECR, utilize `aws ecr get-login-password` para autenticação e ajuste os comandos.

---
### 2️⃣ **Bootstrap da AWS CDK**

O bootstrap configura os recursos necessários para o CDK operar na AWS.

```sh
cdk bootstrap aws://<seu-id-aws>/us-east-1
```

Caso sua infraestrutura use outra região, ajuste o comando acima.

---

### 3️⃣ **Deploy da Infraestrutura**

Após garantir que o bootstrap foi feito corretamente, execute:

```sh
cdk synth

cdk deploy --all --context region=us-east-1
```

Isso criará os recursos como:

- VPC
- Security Group
- Banco de Dados RDS
- Banco de Dados Atlas
- Cluster ECS
- Serviço ECS

---

### 4️⃣ **Atualizar a Aplicação no ECS**

Agora que a imagem está no repositório, precisamos atualizar a aplicação no ECS.

```sh
aws ecs update-service --cluster nava-log-cluster --service nava-log-cluster --force-new-deployment
```

Isso fará com que a aplicação utilize a nova imagem.

---

### 5️⃣ **Testar a Aplicação**

1. Obtenha o endpoint da aplicação no Load Balancer:
   ```sh
   aws elbv2 describe-load-balancers
   ```
2. Acesse o endereço no navegador ou via curl para verificar se a aplicação está rodando:
   ```sh
   curl http://meu-endereco-alb
   ```

---

## 🔄 **Atualizando a Aplicação**

Sempre que precisar atualizar a aplicação:

1. Build da nova versão da imagem Docker
2. Enviar para o repositório (DockerHub ou ECR)
3. Atualizar o serviço no ECS:
   ```sh
   aws ecs update-service --cluster meu-cluster --service meu-servico --force-new-deployment
   ```

---



