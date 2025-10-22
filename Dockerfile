FROM openjdk:21 AS esop-backend
RUN mkdir /app
WORKDIR /app
ENV TZ="Asia/Ho_Chi_Minh"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY /build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=stage", "-jar","/app/app.jar"]
