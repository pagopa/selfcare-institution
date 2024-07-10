# selfcare-institution

This repo structure and build monorepo with Apache Maven for selfcare user domain. 

Applications under apps/ depend on shared code under libs/.
test-coverage/ is used to assess the test coverage of the entire project.



```
.

├── apps
│   ├── institution-send-mail-scheduler
│   └── institution-ms
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
