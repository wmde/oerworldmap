FROM hseeberger/scala-sbt:8u141-jdk_2.12.3_0.13.16

RUN sed -i -e 's/deb.debian.org/archive.debian.org/g' \
           -e 's|security.debian.org|archive.debian.org/|g' \
           -e '/stretch-updates/d' /etc/apt/sources.list

WORKDIR /srv/oerworldmap
