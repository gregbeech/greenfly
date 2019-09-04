package greenfly.aws

import cats._
import cats.implicits._
import org.scalatest.{FlatSpec, MustMatchers}

class AwsTest extends FlatSpec with MustMatchers {

  "A program" should "run" in {
    val program = for {
      vpc <- vpc("test1", Cidr("10.0.0.0/16"))
      sn1 <- subnet(s"${vpc.name}-public", vpc.id, Cidr("10.0.0.0/24"))
      sn2 <- subnet(s"${vpc.name}-private", vpc.id, Cidr("10.0.0.1/24"))
    } yield vpc :: sn1 :: sn2 :: Nil

    val env = Map.empty[String, String]
    val initialState = Map[String, Any]("test1" -> VpcId("existingVpcId"))
    println(s"Initial state is $initialState")

    val (log, state, infra) = program.foldMap(compiler).run(env, initialState).unsafeRunSync()
    println()
    println(log.mkString("\n"))
    println()
    println(infra.mkString("Provisioned ", "\nProvisioned ", ""))
    println()
    println(s"Final state is $state")
  }

  val compiler: AwsOp ~> InfraIO = new (AwsOp ~> InfraIO) {
    def apply[A](fa: AwsOp[A]): InfraIO[A] = fa.compile
  }
}
