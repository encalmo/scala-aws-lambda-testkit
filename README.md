![Maven Central Version](https://img.shields.io/maven-central/v/org.encalmo/scala-aws-lambda-testkit_3?style=for-the-badge)

# scala-aws-lambda-testkit

Artefacts supporting testing of the AWS lambda functions written using Scala 3 and [scala-aws-lambda-runtime](https://github.com/encalmo/scala-aws-lambda-runtime)

## Dependencies

- Scala >= 3.3.5
- [Scala Toolkit 0.7.0](https://github.com/scala/toolkit)
- [munit](https://scalameta.org/munit/)
- [upickle-utils](https://github.com/encalmo/upickle-utils)

## Usage

Use with SBT

    libraryDependencies += "org.encalmo" %% "scala-aws-lambda-testkit" % "0.9.0"

or with SCALA-CLI

    //> using dep org.encalmo::scala-aws-lambda-testkit:0.9.0
