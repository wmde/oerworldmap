FROM node:12
WORKDIR /srv/oerworldmap-ui

ENTRYPOINT [ "/srv/oerworldmap-ui/entrypoint-ui.sh" ]
