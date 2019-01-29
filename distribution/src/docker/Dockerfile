FROM docker-registry.wemove.com/ingrid-java
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-se*-installer.jar /

RUN unzip ingrid-iplug-se*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-se \
    && cp -R /tmp/ingrid-iplug-se-*/* /opt/ingrid/ingrid-iplug-se/ \
    && rm -Rf /tmp/* \
    && rm ingrid-*.jar

RUN echo jetty.port=80 > /opt/ingrid/ingrid-iplug-se/conf/config.override.properties
WORKDIR /opt/ingrid/ingrid-iplug-se
EXPOSE 80

CMD /bin/sh start.sh start
