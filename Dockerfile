FROM docker-evergreen-toolbox-release.dr.corp.adobe.com/java-artifactory:latest

LABEL maintainer "Mark J. Becker <mabecker@adobe.com>"

ENV WORKDIR "/home/qabasel/toughday"
ENV TOUGHDAY_VERSION "0.9.1-SNAPSHOT"
ENV TOUGHDAY_JAR "${WORKDIR}/toughday/target/toughday2.jar"

# Copy repo
RUN mkdir -p /home/qabasel/toughday
COPY --chown=qabasel:qabasel . /home/qabasel/toughday/
WORKDIR ${WORKDIR}

# Build and clean
RUN mvn clean package \
    && rm -rf ~/.m2/repository/ \
    && mv ${WORKDIR}/toughday/target/toughday2-${TOUGHDAY_VERSION}.jar ${TOUGHDAY_JAR}

ENTRYPOINT [ "java", "-jar", "./toughday/target/toughday2.jar" ]