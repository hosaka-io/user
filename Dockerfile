FROM registry.i.hosaka.io/bootstrap
COPY ./target/uberjar/user.jar /srv/user.jar
WORKDIR /srv

EXPOSE 8080

ENTRYPOINT /usr/bin/bootstrap /usr/bin/java -jar /srv/user.jar