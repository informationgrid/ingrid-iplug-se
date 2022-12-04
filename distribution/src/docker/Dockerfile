FROM alpine AS builder

COPY ingrid-iplug-se*-installer.jar /
RUN unzip -q ingrid-iplug-se*-installer.jar -d /tmp \
    && export INSTALL_DIR=`ls -d1 /tmp/ingrid-iplug-se-*` \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' $INSTALL_DIR/conf/log4j2.xml \
    && echo jetty.port=8080 > $INSTALL_DIR/conf/config.override.properties

FROM docker-registry.wemove.com/ingrid-java:jre-11
ENV INGRID_USER=ingrid
ENV RUN_DIRECTLY=true

# user, group ingrid (1000:1000) is created in base image
COPY --chown=1000:1000 --from=builder /tmp/ingrid-iplug-se-* /opt/ingrid/ingrid-iplug-se/

USER ingrid

WORKDIR /opt/ingrid/ingrid-iplug-se
EXPOSE 8080

CMD /bin/sh start.sh start
