####
# This Dockerfile is used in order to build a container that runs the application in JVM mode
#
# Before building the container image run:
#
# > ./gradlew bootJar
#
# Then, build the image with:
#
# > docker build -t hubsuimz/chatgpt-web-java .
#
# Then run the container using:
#
# > docker run --name chatgpt-web-java -d -p 8080:8080 -v ~/chatgpt-web-java:/app/config -e "JAVA_OPTS=-Xms256m -Xmx256m" hubsuimz/chatgpt-web-java
#
# Next, you need to modify the `app.openai-api-key` or `app.openai-access-token` in the ~/chatgpt-web-java/application-app.properties file, and finally restart the container:
#
# > docker restart chatgpt-web-java
#
# Or:
#
# > docker run --name chatgpt-web-java -d -p 8080:8080 hubsuimz/chatgpt-web-java --app.openai-api-key=sk-xxx
#
# You can configure the behavior using the following environment properties:
# - JAVA_OPTS: JVM options passed to the `java` command (example: -e "JAVA_OPTS=-Xms256m -Xmx256m")
#
# You can configure the docker run command properties: (example: --app.openai-api-key=sk-xxx)
# - app.auth-secret-key: ChatGPT-Web front-end authorization key
#
# - app.openai-api-key: OpenAI API Key, https://platform.openai.com/overview
# - app.openai-api-base-url: openai api url, defualt: https://api.openai.com
# - app.openai-api-mode: gpt-4, gpt-4-0314, gpt-4-32k, gpt-4-32k-0314, gpt-3.5-turbo(default), gpt-3.5-turbo-0301, text-davinci-003, text-davinci-002, code-davinci-002
# - app.openai-sensitive-id: Used to query balance, change this to an `sensitiveId` extracted from the ChatGPT site's `https://platform.openai.com/account/usage`
#
# - app.openai-access-token: Set this to an `accessToken` extracted from the ChatGPT site's `https://chat.openai.com/api/auth/session` response
# - app.openai-reverse-api-proxy-url: Reverse Proxy - Available on accessToken, default: https://bypass.churchless.tech/api/conversation
#
# - app.api-timeout-ms: API request timeout-ms, default: 120000
# - app.max-request-per-hour: Chat API maximum number of requests per hour, 0 - unlimited(default)
#
# - app.socks-proxy.host=
# - app.socks-proxy.port=
# - app.socks-proxy.username=
# - app.socks-proxy.password=
#
# - app.http-proxy.host=
# - app.http-proxy.port=
####
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="suimz/chatgpt-web-java<https://github.com/suimz/chatgpt-web-java>"
LABEL version="0.0.1"

VOLUME /tmp
ADD build/libs/app.jar /app/app.jar
WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx256m"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --spring.config.additional-location=/app/config/application-app.properties ${0} ${@}"]
