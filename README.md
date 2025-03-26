<a href="https://github.com/encalmo/scala-aws-lambda-testkit">![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)</a> <a href="https://central.sonatype.com/artifact/org.encalmo/scala-aws-lambda-testkit_3" target="_blank">![Maven Central Version](https://img.shields.io/maven-central/v/org.encalmo/scala-aws-lambda-testkit_3?style=for-the-badge)</a> <a href="https://encalmo.github.io/scala-aws-lambda-testkit/scaladoc/org/encalmo/lambda.html" target="_blank"><img alt="Scaladoc" src="https://img.shields.io/badge/docs-scaladoc-red?style=for-the-badge"></a>

# scala-aws-lambda-testkit

Artefacts supporting testing of the AWS lambda functions written using Scala 3 and [scala-aws-lambda-runtime](https://github.com/encalmo/scala-aws-lambda-runtime)

## Table of contents

- [Dependencies](#dependencies)
- [Usage](#usage)
- [Project content](#project-content)

## Dependencies

   - [Scala](https://www.scala-lang.org) >= 3.3.5
   - [Scala **toolkit** 0.7.0](https://github.com/scala/toolkit)
   - org.encalmo [**upickle-utils** 0.9.3](https://central.sonatype.com/artifact/org.encalmo/upickle-utils_3)
   - org.scalameta [**munit** 1.1.0](https://github.com/scalameta/munit)

## Usage

Use with SBT

    libraryDependencies += "org.encalmo" %% "scala-aws-lambda-testkit" % "0.9.1"

or with SCALA-CLI

    //> using dep org.encalmo::scala-aws-lambda-testkit:0.9.1


## Project content

```
├── .github
│   └── workflows
│       ├── pages.yaml
│       ├── release.yaml
│       └── test.yaml
│
├── .gitignore
├── .scalafmt.conf
├── JsonAssertions.scala
├── LambdaServiceFixture.scala
├── LICENSE
├── project.scala
├── README.md
├── StubbedHttpServer.scala
├── StubbedHttpServer.test.scala
├── StubbedProxyHttpServer.scala
├── StubbedProxyHttpServer.test.scala
└── test.sh
```

