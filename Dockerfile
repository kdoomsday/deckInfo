FROM azul/zulu-openjdk:17-jre-latest

ARG VERSION=1.1

EXPOSE 8080/tcp
RUN mkdir /opt/app
WORKDIR /opt/app/deckinfo-$VERSION
ADD /out/tapir/assembly.dest/out.jar .
ADD /dbInitScripts/ dbInitScripts/
ADD /cards.xml .
ADD /www/ www/
# CMD ["bin/deckInfo"]
CMD ["java", "-jar", "out.jar"]
