FROM openjdk:11-jre as zeebe-cluster-testbench

USER nobody

COPY core/target/zeebe-cluster-testbench-uber-jar-with-dependencies.jar /testbench.jar

CMD java $JAVA_OPTIONS -jar testbench.jar