package org.encalmo.lambda

import ujson.*
import munit.Location
import scala.collection.immutable.ArraySeq

object JsonAssertions extends munit.Assertions {

  extension (value: Value) {

    final def getStringAt(
        path: String
    )(using Location): String =
      lookupJsonPath(value, path) match {
        case Str(value) => value
        case other =>
          fail(
            s"Expected JSON string at path [$path] but found $other"
          )
      }

    final def getIntAt(
        path: String
    )(using Location): Int =
      lookupJsonPath(value, path) match {
        case Num(value) =>
          try { value.toInt }
          catch {
            case e: Exception =>
              fail(
                s"Expected integer at path [$path] but found $value"
              )
          }
        case other =>
          fail(
            s"Expected JSON number at path [$path] but found $other"
          )
      }

    final def getStringOrIntAt(
        path: String
    )(using Location): Int =
      lookupJsonPath(value, path) match {
        case Str(value) =>
          try { value.toInt }
          catch {
            case e: Exception =>
              fail(
                s"Expected integer string at path [$path] but found $value"
              )
          }
        case Num(value) =>
          try { value.toInt }
          catch {
            case e: Exception =>
              fail(
                s"Expected integer number at path [$path] but found $value"
              )
          }
        case other =>
          fail(
            s"Expected JSON integer (string or number) at path [$path] but found $other"
          )
      }

    final def getBooleanAt(
        path: String
    )(using Location): Boolean =
      lookupJsonPath(value, path) match {
        case Bool(value) => value
        case other =>
          fail(
            s"Expected JSON boolean at path [$path] but found $other"
          )
      }

    final def getDoubleAt(
        path: String
    )(using Location): Double =
      lookupJsonPath(value, path) match {
        case Num(value) => value
        case other =>
          fail(
            s"Expected JSON number at path [$path] but found $other"
          )
      }

    final def getBigDecimalAt(
        path: String
    )(using Location): BigDecimal =
      lookupJsonPath(value, path) match {
        case Num(value) => BigDecimal(value)
        case Str(value) => BigDecimal(value)
        case other =>
          fail(
            s"Expected JSON decimal at path [$path] but found $other"
          )
      }

    final def assertObjectExistsAt(
        path: String
    )(using Location): Value =
      lookupJsonPath(value, path) match {
        case obj: Obj => value
        case other =>
          fail(
            s"Expected JSON object at path [$path] but found $other"
          )
      }

    final def assertObjectAt(
        path: String
    )(check: Obj => Unit)(using Location): Value =
      lookupJsonPath(value, path) match {
        case obj: Obj => check(obj); value
        case other =>
          fail(
            s"Expected JSON object at path [$path] but found $other"
          )
      }

    final def assertObjectIfExistsAt(
        path: String
    )(check: Obj => Unit)(using Location): Value =
      lookupJsonPath(value, path) match {
        case obj: Obj => check(obj); value
        case other    => value
      }

    final def assertStringExistsAt(path: String)(using
        Location
    ): Value =
      value.getStringAt(path)
      value

    final def assertStringAt(path: String)(expected: String)(using
        Location
    ): Value =
      val arg = value.getStringAt(path)
      assertEquals(
        arg,
        expected,
        s"string `$expected` expected at path [$path] but got '$arg'"
      )
      value

    final def assertStringAt(path: String)(check: String => Boolean)(using
        Location
    ): Value =
      val arg = value.getStringAt(path)
      assert(
        check(arg),
        s"valid string expected at path [$path] but got '$arg'"
      )
      value

    final def assertBooleanExistsAt(path: String)(using
        Location
    ): Value =
      value.getBooleanAt(path)
      value

    final def assertBooleanAt(path: String)(expected: Boolean)(using
        Location
    ): Value =
      val arg = value.getBooleanAt(path)
      assertEquals(
        arg,
        expected,
        s"boolean `$expected` expected at at path [$path] but got '$arg'"
      )
      value

    final def assertTrueAt(path: String)(using
        Location
    ): Value =
      val arg = value.getBooleanAt(path)
      assertEquals(
        arg,
        true,
        s"boolean `true` expected at at path [$path] but got '$arg'"
      )
      value

    final def assertFalseAt(path: String)(using
        Location
    ): Value =
      val arg = value.getBooleanAt(path)
      assertEquals(
        arg,
        false,
        s"boolean `false` expected at at path [$path] but got '$arg'"
      )
      value

    final def assertIntExistsAt(path: String)(using
        Location
    ): Value =
      value.getIntAt(path)
      value

    final def assertIntAt(path: String)(expected: Int)(using
        Location
    ): Value =
      val arg = value.getIntAt(path)
      assertEquals(
        arg,
        expected,
        s"integer `$expected` expected at path [$path] but got '$arg'"
      )
      value

    final def assertIntStringAt(path: String)(expected: Int)(using
        Location
    ): Value =
      val arg = value.getStringOrIntAt(path)
      assertEquals(
        arg,
        expected,
        s"integer `$expected` expected at path [$path] but got '$arg'"
      )
      value

    final def assertDoubleExistsAt(path: String)(using
        Location
    ): Value =
      value.getDoubleAt(path)
      value

    final def assertDoubleAt(path: String)(expected: Double)(using
        Location
    ): Value =
      val arg = value.getDoubleAt(path)
      assertEquals(
        arg,
        expected,
        s"number `$expected` expected at path [$path] but got '$arg'"
      )
      value

    final def assertBigDecimalExistsAt(path: String)(using
        Location
    ): Value =
      value.getBigDecimalAt(path)
      value

    final def assertBigDecimalAt(path: String)(expected: BigDecimal)(using
        Location
    ): Value =
      val arg = value.getBigDecimalAt(path)
      assertEquals(
        arg,
        expected,
        s"decimal `$expected` expected at path [$path] but got '$arg'"
      )
      value

    final def assertBigDecimalAt2(path: String)(check: BigDecimal => Boolean)(using
        Location
    ): Value =
      val arg = value.getBigDecimalAt(path)
      assert(
        check(arg),
        s"valid decimal expected at path [$path] but got '$arg'"
      )
      value

    private def lookupJsonPath(
        current: Value,
        path: String
    )(using Location): Value =
      lookupJsonPath(
        current,
        ArraySeq.unsafeWrapArray(path.split("\\.")),
        Seq.empty
      )

    private def lookupJsonPath(
        current: Value,
        path: Seq[String],
        breadcrumbs: Seq[String]
    )(using Location): Value = {
      current match {
        case Obj(map) =>
          path.headOption match {
            case Some(name) =>
              if (map.contains(name)) {
                val nestedValue = map(name)
                if (path.tail.isEmpty)
                  nestedValue
                else
                  lookupJsonPath(nestedValue, path.tail, breadcrumbs :+ name)
              } else
                fail(
                  s"JSON object${
                      if (breadcrumbs.isEmpty) ""
                      else s" at ${breadcrumbs.mkString(".")}"
                    } does NOT contain property [$name], see:\n${ujson
                      .write(current, indent = 2)}"
                )

            case None =>
              current
          }

        case other =>
          fail(
            s"Expected JSON object at path ${breadcrumbs.mkString(".")} but found $other"
          )
      }
    }

    private def lookupMaybeJsonPath(
        current: Value,
        path: String
    )(using Location): Option[Value] =
      lookupMaybeJsonPath(
        current,
        ArraySeq.unsafeWrapArray(path.split("\\.")),
        Seq.empty
      )

    private def lookupMaybeJsonPath(
        current: Value,
        path: Seq[String],
        breadcrumbs: Seq[String]
    )(using Location): Option[Value] = {
      current match {
        case Obj(map) =>
          path.headOption match {
            case Some(name) =>
              if (map.contains(name)) {
                val nestedValue = map(name)
                if (path.tail.isEmpty)
                  Some(nestedValue)
                else
                  lookupMaybeJsonPath(nestedValue, path.tail, breadcrumbs :+ name)
              } else
                None

            case None =>
              Some(current)
          }

        case other =>
          None
      }
    }
  }
}
