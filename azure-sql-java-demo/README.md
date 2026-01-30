# Azure SQL Java Demo (Spring Boot, Java 17)

A minimal to-do style app:
- Backend: Java 17, Spring Boot (Web, Data JPA)
- DB: Azure SQL Database (via Microsoft JDBC driver)
- UI: Simple HTML + CSS + buttons (no JS)

## Prerequisites
- Java 17 (Temurin/Oracle/OpenJDK)
- Maven 3.8+
- An Azure subscription + Azure CLI (`az`) logged in: `az login`

---

## 1) Configure Azure SQL
Create a resource group, SQL server, and database (names can be changed):

```bash
az group create -n rg-azure-sql-demo -l centralindia

# Create logical SQL server (use your own unique admin and password)
az sql server create   -g rg-azure-sql-demo -n sqlsrv-azure-sql-demo   -u sqladmin -p "P@ssw0rd-ChangeMe" -l centralindia

# Allow your current public IP to connect
az sql server firewall-rule create   -g rg-azure-sql-demo -s sqlsrv-azure-sql-demo   -n allow-my-ip --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0

# Create the database
az sql db create -g rg-azure-sql-demo -s sqlsrv-azure-sql-demo -n sqldb-azure-sql-demo --service-objective Basic
```

> Tip: For local development from dynamic IPs, you might temporarily set start/end to your current IP. Setting 0.0.0.0 allows Azure services; restrict for production.

Construct your JDBC URL (replace placeholders):

```
jdbc:sqlserver://sqlsrv-azure-sql-demo.database.windows.net:1433;database=sqldb-azure-sql-demo;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

Export environment variables for local run (PowerShell syntax shown first, then Bash):

**PowerShell**
```powershell
$env:SPRING_DATASOURCE_URL="<your-jdbc-url>"
$env:SPRING_DATASOURCE_USERNAME="sqladmin"
$env:SPRING_DATASOURCE_PASSWORD="P@ssw0rd-ChangeMe"
```

**Bash**
```bash
export SPRING_DATASOURCE_URL="<your-jdbc-url>"
export SPRING_DATASOURCE_USERNAME="sqladmin"
export SPRING_DATASOURCE_PASSWORD="P@ssw0rd-ChangeMe"
```

---

## 2) Run locally
```bash
mvn -q -DskipTests spring-boot:run
# or build a jar
mvn -q -DskipTests clean package
java -jar target/azure-sql-java-demo-0.0.1-SNAPSHOT.jar
```
App runs on <http://localhost:8080>.

Hibernate will auto-create the `Task` table because `spring.jpa.hibernate.ddl-auto=update` is enabled.

---

## 3) Deploy to Azure App Service (Java SE 17)

Create an App Service plan + web app (Linux):
```bash
# App Service plan
az appservice plan create -g rg-azure-sql-demo -n asp-azure-sql-demo --sku B1 --is-linux

# Web app (use a globally unique app name)
APP_NAME="azure-sql-java-demo-$RANDOM"
az webapp create -g rg-azure-sql-demo -p asp-azure-sql-demo -n $APP_NAME --runtime "JAVA:17-java17"
```

Configure app settings for the connection string:
```bash
az webapp config appsettings set -g rg-azure-sql-demo -n $APP_NAME --settings  SPRING_DATASOURCE_URL="<your-jdbc-url>"  SPRING_DATASOURCE_USERNAME="sqladmin"  SPRING_DATASOURCE_PASSWORD="P@ssw0rd-ChangeMe"
```

Build and deploy the jar:
```bash
mvn -q -DskipTests clean package
az webapp deploy -g rg-azure-sql-demo -n $APP_NAME --type jar --src-path target/azure-sql-java-demo-0.0.1-SNAPSHOT.jar
```

Browse your app:
```
echo https://$APP_NAME.azurewebsites.net
```

> If the `--runtime` string changes in the future, you can list available Linux runtimes:
> `az webapp list-runtimes --linux -o table`

---

## 4) Troubleshooting
- **Startup errors**: Check logs
  ```bash
  az webapp log config -g rg-azure-sql-demo -n $APP_NAME --docker-container-logging filesystem
  az webapp log tail -g rg-azure-sql-demo -n $APP_NAME
  ```
- **DB connectivity**: Ensure SQL firewall allows Azure services or the outbound IPs of your web app. In the portal, add a firewall rule for the web app outbound IPs (found under *Properties* of the web app) or enable the setting "Allow Azure services and resources to access this server" on the SQL server.
- **Migrations**: This sample uses `ddl-auto=update`. For production, prefer Flyway/Liquibase.

---

## 5) Optional: Containerize
Create an image with the included `Dockerfile` and run in Azure Web App for Containers or Azure Container Apps.

```bash
# Build jar first
mvn -q -DskipTests clean package

# Build container image
IMAGE="azure-sql-java-demo:local"
docker build -t $IMAGE .

docker run -p 8080:8080   -e SPRING_DATASOURCE_URL="<your-jdbc-url>"   -e SPRING_DATASOURCE_USERNAME="sqladmin"   -e SPRING_DATASOURCE_PASSWORD="P@ssw0rd-ChangeMe"   $IMAGE
```