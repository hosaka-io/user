FROM registry.i.hosaka.io/bootstrap
COPY ./target/uberjar/user.jar /srv/user.jar
WORKDIR /srv

EXPOSE 8080 8079

ENTRYPOINT /usr/bin/bootstrap /usr/bin/java -Xms128m -Xmx512m -jar /srv/user.jar