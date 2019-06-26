FROM java

COPY . .
EXPOSE 3000

CMD [ "java", "-jar", "/out/artifacts/NetworkedGeneticAlgorithm_jar/NetworkedGeneticAlgorithm.jar" ]