# Selfcare Institution

This repo structure and build monorepo with Apache Maven for selfcare institution domain.

test-coverage/ is used to assess the test coverage of the entire project.


```
.

├── apps
│   ├── user-institution-send-mail-scheduler
└── test-coverage
```

Look at single README module for more information.

## Infrastructure

The [`.container_apps/`] sub folder contains terraform files for deploying infrastructure as container apps in Azure.


## Continous integration

The [`.github/`] sub folder contains a self-contained ci-stack for building the monorepo with Github Actions.

## Usage

```shell script
mvn clean package install
```

## Maven basic actions for monorep

Maven is really not a monorepo-*native* build tool (e.g. lacks
trustworthy incremental builds, can only build java code natively, is recursive and
struggles with partial repo checkouts) but can be made good use of with some tricks
and usage of a couple of lesser known command line switches.

| Action                                                       |           in working directory           | with Maven                                                                                                 |
|:-------------------------------------------------------------|:----------------------------------------:|:-----------------------------------------------------------------------------------------------------------|
| Build the world                                              |                   `.`                    | `mvn clean package -DskipTests`                                                                            |
| Run `institution-send-mail-scheduler`                        |                   `.`                    | `java -jar apps/institution-send-mail-scheduler/target/institution-send-mail-scheduler-1.0.0-SNAPSHOT.jar` |
| Build and test the world                                     |               `.`                        | `mvn clean package`                                                                                        |
| Build the world                                              | `./apps/institution-send-mail-scheduler` | `mvn --file ../.. clean package -DskipTests`                                                               |
| Build `institution-send-mail-scheduler` and its dependencies |                   `.`                    | `mvn --projects :institution-send-mail-scheduler  --also-make clean package -DskipTests`                   |
| Build `institution-send-mail-scheduler` and its dependencies | `./apps/institution-send-mail-scheduler` | `mvn --file ../.. --projects :institution-send-mail-scheduler --also-make clean package -DskipTests`       |
