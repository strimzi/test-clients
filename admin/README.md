# KafkaAdmin client CLI tool

CLI tool for operating topics inside Kafka.

It can be used for:

* creating
* listing
* altering
* deleting
* describing

## Usage

The CLI tool can be currently used only inside of the test-clients Docker image.

You can pull the test-clients image and exec into it:

```bash
docker run -it --entrypoint bash quay.io/strimzi/test-clients:latest
```
CLI tool is already in `$PATH`, so you can start with running:

```
> admin-client
Missing required subcommand
Usage: admin-client [COMMAND]
Commands:
  topic
  configure

> admin-client topic
Usage: admin-client topic [COMMAND]
Commands:
  create
  delete
  list
  describe
  alter

> admin-client topic list -h
Usage: admin-client topic list [-h] --bootstrap-server=<bootstrapServer>
      --bootstrap-server=<bootstrapServer>
               Bootstrap server address
  -h, --help   Display this help message
```

`create`, `delete`, `describe`, `alter` can be used with one topic, or multiple topics matching prefix
(with specified topics count). `list` will show all topics present in Kafka, there is no filtering of topics based on
name.

## Configuring the Admin client

You can configure the Admin client before any other operation.
That can be done using `configure` subcommand:

```
> admin-client configure
Missing required subcommand
Usage: admin-client configure [COMMAND]
Commands:
  oauth
  sasl
  ssl
  common
```

In total there are 4 subcommands that you can use for the configuration.
All of the subcommands have their respective options with description what you can configure.
The `common` subcommands has options that allow loading configuration from environment variables (env. variables are same as for the other clients)
or from properties file (the properties' keys are same as the names of env. variables, only difference is that the keys are in lower case and with
`.` instead of `_` -> `BOOTSTRAP_SERVERS` environment variable is `bootstrap.servers` property in the properties file).

The configuration of the Admin client is then stored in `~/.admin-client/config.properties` file.

In case you want to store the configuration file in different location, you can specify the `CONFIG_FOLDER_PATH` where the `.admin-client`
folder with the `config.properties` file will be created.

## Local development & testing

In case you want to build the CLI tool locally, I recommend to install GraalVM and run
```bash
make prepare_admin_files
cd docker-images/tmp/admin-client
native-image -jar admin.jar admin-client
```

You can use the `make` target:
```bash
make build_admin_cli
```
that build the application using `ghcr.io/graalvm/graalvm-community` image in Docker container, 
but you can experience issues with platform dependencies etc., that's why I recommend installing GraalVM on your
machine and building it completely locally.