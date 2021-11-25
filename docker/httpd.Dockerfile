FROM httpd:latest

COPY docker/vhost.docker.conf /usr/local/apache2/conf/extra/oerworldmap.conf
COPY docker/httpd.conf /usr/local/apache2/conf/httpd.conf
