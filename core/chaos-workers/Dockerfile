FROM openjdk:11-jre

ARG TOKEN
ENV ZEEBE_VERSION=0.26.0 \
    CHAOS_HOME=/home/chaos \
    CONTEXT=gke_camunda-cloud-240911_europe-west1-d_ultrachaos

WORKDIR ${CHAOS_HOME}
COPY chaos-worker/target/chaos-worker-*-jar-with-dependencies.jar chaosWorker.jar
# COPY handleJob.sh .
# COPY runWorker.sh .
# COPY handlerUtil.sh .
COPY installDependencies.sh .

ENV PATH "${CHAOS_HOME}:${PATH}"

#ENV TINI_VERSION=v0.19.0
#ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /usr/bin/tini
#RUN chmod +x -R /usr/bin/tini
#COPY /home/zell/playground/zbctl /usr/local/bin/zbctl
# ADD https://github.com/zeebe-io/zeebe/releases/download/${ZEEBE_VERSION}/zbctl /usr/local/bin/zbctl
# RUN chmod +x /usr/local/bin/zbctl

# install dependencies
RUN ${CHAOS_HOME}/installDependencies.sh
RUN git clone https://github.com/zeebe-io/zeebe-chaos.git

# current user is root so we need to copy it to root
# probably we want to create a different user
RUN mkdir -p .kube
COPY kubeconfig /root/.kube/config
RUN kubectl config set-credentials ${CONTEXT}-zeebe-chaos-token-user --token ${TOKEN}
RUN kubectl config set-context ${CONTEXT} --user ${CONTEXT}-zeebe-chaos-token-user

# ./runWorker.sh
CMD java -jar chaosWorker.jar
