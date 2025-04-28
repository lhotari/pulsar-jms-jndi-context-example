# Minimal Pulsar JMS example using JNDI InitialContext

[Source code](src/main/java/com/example/App.java)

### Running Pulsar Broker in Docker

Starting

```shell
docker run -d --rm --name pulsar \
  -p 6650:6650 -p 8080:8080 \
  apachepulsar/pulsar:latest \
  bin/pulsar standalone -nss -nfw
```

Stopping later
```shell
docker stop pulsar
```

### Running the example

```shell
mvn package
java -jar target/jmstest-1.0-SNAPSHOT-jar-with-dependencies.jar
```