FROM ubuntu:latest
LABEL authors="kirillzhukov"

ENTRYPOINT ["top", "-b"]