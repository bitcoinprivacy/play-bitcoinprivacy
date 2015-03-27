FROM      ubuntu
MAINTAINER Jorge Martinez Pizarro and Stefan Richter <info@bitcoinprivacy.net>
EXPOSE 9000 8888

RUN apt-get update && apt-get install -y wget software-properties-common git emacs24-nox
RUN git config --global user.email "info@bitcoinprivacy.net"
RUN git config --global user.name "Jorge or Stefan"
RUN add-apt-repository ppa:webupd8team/java

#this is to automatically accept the oracle license
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections

RUN apt-get update && apt-get install -y oracle-java8-installer

RUN wget https://dl.bintray.com/sbt/native-packages/sbt/0.13.8/sbt-0.13.8.tgz
RUN tar xzvf sbt-0.13.8.tgz
ENV PATH sbt:$PATH

WORKDIR /root/
RUN git clone https://github.com/stefanwouldgo/play-bitcoinprivacy.git
RUN cd play-bitcoinprivacy && sbt compile

# link to .emacs files and evaluate once to install all emacs packages
RUN ln -s play-bitcoinprivacy/playdocker/root/.emacs* .
RUN emacs -batch .emacs --eval '(eval-buffer)'