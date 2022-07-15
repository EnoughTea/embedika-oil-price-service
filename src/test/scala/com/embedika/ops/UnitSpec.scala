package com.embedika.ops

import org.scalatest.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.*
import org.scalatest.matchers.*


trait CommonTestFeatures extends should.Matchers with OptionValues with Inside with Inspectors with ScalaFutures

abstract class UnitSpec extends AnyFlatSpec with CommonTestFeatures
