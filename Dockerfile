FROM azul/zulu-openjdk:17-jre-latest
EXPOSE 8080/tcp
ARG VERSION=1.1
RUN mkdir /opt/app
WORKDIR /opt/app/deckinfo-$VERSION
ADD out/zioWeb/assembly.dest/out.jar .
# RUN apt-get update
# RUN apt-get install unzip
# RUN unzip /opt/app/deckinfo-$VERSION.zip -d /opt/app
ADD dbInitScripts/ dbInitScripts/
ADD cards.xml .
# CMD ["bin/deckInfo"]
CMD ["java", "-jar", "out.jar"]
