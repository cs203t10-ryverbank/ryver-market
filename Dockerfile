FROM openjdk:11-jre
WORKDIR /ryver-market
COPY . /app
ENTRYPOINT ["java","-jar","/app/ryver-market/target/market-0.0.1-SNAPSHOT.jar"]