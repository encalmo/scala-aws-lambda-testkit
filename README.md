# scala-aws-lambda-testkit

Artefacts supporting testing of the AWS lambda functions written using Scala 3 and [scala-aws-lambda-runtime](https://github.com/encalmo/scala-aws-lambda-runtime)

## Dependencies

- Scala >= 3.6.3
- [munit](https://scalameta.org/munit/)
- [upickle-utils](https://github.com/encalmo/upickle-utils)

## Usage

Use with SBT

    libraryDependencies += "org.encalmo" %% "scala-aws-lambda-testkit" % "0.9.0"

or with SCALA-CLI

    //> using dep org.encalmo::scala-aws-lambda-testkit:0.9.0