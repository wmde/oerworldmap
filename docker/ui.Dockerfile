FROM node:12.22.7
WORKDIR /srv/oerworldmap-ui

COPY docker/entrypoint-ui.sh /srv

ENTRYPOINT [ "/srv/entrypoint-ui.sh" ]
