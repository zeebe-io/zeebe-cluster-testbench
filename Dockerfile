FROM openjdk:11-jre as zeebe-cluster-testbench

COPY core/target/zeebe-cluster-testbench-uber-jar-with-dependencies.jar /testbench.jar

COPY workflows/ workflows/

CMD java $JAVA_OPTIONS -jar testbench.jar