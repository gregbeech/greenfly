package greenfly

import cats.data._
import cats.free.Free
import cats.implicits._
import greenfly.aws._
import greenfly.datadog._
import org.scalatest.{FlatSpec, MustMatchers}

class BasicTest extends FlatSpec with MustMatchers {
  type TestApp[A] = EitherK[AwsOp, DatadogOp, A]

  def program(implicit aws: AwsOps[TestApp], datadog: DatadogOps[TestApp]): Free[TestApp, Unit] = {
    import aws._
    import datadog._

    for {
      vpc <- vpc("test1", Cidr("10.0.0.0/16"))
      _   <- dashboard(s"${vpc.name} dashboard")
      _   <- subnet(s"${vpc.name}-public", vpc.id, Cidr("10.0.0.0/24"))
      _   <- subnet(s"${vpc.name}-private", vpc.id, Cidr("10.0.0.1/24"))
    } yield ()
  }

  "A program" should "run" in {
    val p = program
    val compiler = AwsCompiler or DatadogCompiler

    val env = Map.empty[String, String]
    val initialState = Map[String, String]("test1" -> "existingVpcId")
    val (log, state, _) = p.foldMap(compiler).run(env, initialState).unsafeRunSync()

    println(s"Initial state was $initialState")
    println(log.mkString("\n"))
    println(s"Final state is $state")
  }
}
