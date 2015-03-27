FROM      ubuntu
MAINTAINER Jorge Martinez Pizarro and Stefan Richter <info@bitcoinprivacy.net>
EXPOSE 9000 8888

RUN apt-get update && apt-get install -y wget unzip software-properties-common git emacs24-nox
RUN git config --global user.email "info@bitcoinprivacy.net"
RUN git config --global user.name "Jorge or Stefan"
RUN add-apt-repository ppa:webupd8team/java

#this is to automatically accept the oracle license
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections

RUN apt-get update && apt-get install -y oracle-java8-installer

RUN wget http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2-minimal.zip
RUN unzip typesafe-activator-1.3.2-minimal
RUN mv activator-1.3.2-minimal/activator* /bin/
#this might be a little more elegant using ENV

WORKDIR /root/
RUN git clone https://github.com/stefanwouldgo/play-bitcoinprivacy.git
RUN cd play-bitcoinprivacy && activator compile

# link to .emacs files and evaluate once to install all emacs packages
RUN ln -s play-bitcoinprivacy/playdocker/root/.emacs* .
RUN emacs -batch .emacs --eval '(eval-buffer)'