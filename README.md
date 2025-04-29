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

### Alternative: Connecting to StreamNative Cloud and authenticating with an API key

### Specifying the connection parameters in environment variables

First create a file `set_env.sh` with the following content:

```shell
export PULSAR_SERVICE_URL=service url in the format pulsar+ssl://<DNS name>:6651
export PULSAR_WEBSERVICE_URL=webservice url in the format https://<DNS name>
export PULSAR_AUTH_TOKEN=<StreamNative Cloud API key>
```
Replace the values with your own. You can find them in the StreamNative Console in "Pulsar Clients".
Start the wizard to connect with the Java client and an API key. The wizard will show you the values to use.

Here's example content:

```shell
export PULSAR_SERVICE_URL=pulsar+ssl://pc-12345678.example.streamnative.aws.snio.cloud:6651
export PULSAR_WEBSERVICE_URL=https://pc-12345678.example.streamnative.aws.snio.cloud
export PULSAR_AUTH_TOKEN=<about 1000 character string here>
```

Then run the following command to set the environment variables:

```shell
source set_env.sh
```

Then you can run the example.

### Running the example

```shell
mvn package
java -jar target/jmstest-1.0-SNAPSHOT-jar-with-dependencies.jar
```