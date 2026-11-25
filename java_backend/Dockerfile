# ============================================================
# Java Web Application (Tomcat 9) Dockerfile
# ============================================================
FROM tomcat:9.0-jdk17-temurin

# Remove default ROOT application
RUN rm -rf /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/docs /usr/local/tomcat/webapps/examples

# Copy pre-built production WAR into Tomcat webapps
COPY target/RetailIQ.war /usr/local/tomcat/webapps/RetailIQ.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
