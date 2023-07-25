FROM eclipse-temurin:17.0.8_7-jre as zeebe-cluster-testbench

COPY core/target/zeebe-cluster-testbench-uber-jar-with-dependencies.jar /testbench.jar

COPY processes/ processes/

CMD java $JAVA_OPTIONS -jar testbench.jar
