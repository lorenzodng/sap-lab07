#file docker
#serve a costruire un'immagine docker:
#- immagine di partenza (contiene maven 3.9.9 e java 21)
#- directory di lavoro (è una cartella del container in cui verrà eseguito il codice del microservizio)
#- copia tutto il codice del progetto nella directory di lavoro
#- compila il progetto
#- porta di esecuzione del microservizio (facoltativo)
#- costruisce l'esecuzione (["strumento di build", "comando maven per eseguire il codice", "classe main da avviare"])
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /project
COPY . /project/
RUN mvn clean package
EXPOSE 9000
CMD ["mvn", "exec:java", "-Dexec.mainClass=ttt_account_service.infrastructure.AccountServiceMain"]