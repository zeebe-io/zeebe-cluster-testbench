FROM eclipse-temurin:21.0.4_7-jre as zeebe-cluster-testbench

COPY core/target/zeebe-cluster-testbench-uber-jar-with-dependencies.jar /testbench.jar

COPY processes/ processes/

CMD java $JAVA_OPTIONS -jar testbench.jar
