FROM broadinstitute/openidc-proxy AS openidc

FROM httpd:2.4.51-buster

RUN apt-get update \
    && apt-get install -y \
        libcjose0 \
        libjansson4 \
        libjq1 \
    && rm -rf /var/lib/apt/lists
COPY --from=openidc /usr/lib/apache2/modules/mod_auth_openidc.so /usr/local/apache2/modules/
COPY docker/vhost.docker.conf /usr/local/apache2/conf/extra/oerworldmap.conf
COPY docker/httpd.conf /usr/local/apache2/conf/httpd.conf
