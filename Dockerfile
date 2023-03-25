FROM eclipse-temurin:11
EXPOSE 9000/tcp
RUN mkdir /opt/app
ADD playWeb/target/universal/playweb-1.0.zip /opt/app
RUN apt-get update
RUN apt-get install unzip
RUN unzip /opt/app/playweb-1.0.zip -d /opt/app
WORKDIR /opt/app/playweb-1.0
ADD dbInitScripts/ dbInitScripts/
ADD cards.xml .
CMD ["bin/playweb", "-Dplay.http.secret.key=$(echo $RANDOM | md5sum | head -c 20)"]
