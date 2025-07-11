FROM public.ecr.aws/amazoncorretto/amazoncorretto:17
COPY ./application/build/libs/application-1.0.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application-1.0.jar"]