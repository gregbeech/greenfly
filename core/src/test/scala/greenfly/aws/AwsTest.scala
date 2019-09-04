package greenfly.aws

import java.util.UUID

import cats._
import cats.data._
import org.scalatest.{FlatSpec, MustMatchers}

class AwsTest extends FlatSpec with MustMatchers {

  "A program" should "run" in {
    val program = for {
      vpc <- vpc("test1", Cidr("10.0.0.0/16"))
      sn1 <- subnet(s"${vpc.name}-public", vpc.id, Cidr("10.0.0.0/24"))
      sn2 <- subnet(s"${vpc.name}-private", vpc.id, Cidr("10.0.0.1/24"))
    } yield vpc :: sn1 :: sn2 :: Nil

    val initialState = Map[String, Any]("test1" -> VpcId("existingVpcId"))
    println(s"Initial state is $initialState")
    println()
    val (state, output) = program.foldMap(pureCompiler).run(initialState).value
    println()
    println(output.mkString("Provisioned ", "\nProvisioned ", ""))
    println()
    println(s"Final state is $state")
  }

  type StateMap = Map[String, Any]
  type GreenflyState[A] = State[StateMap, A]

  val pureCompiler: AwsOp ~> GreenflyState = new (AwsOp ~> GreenflyState) {
    def apply[A](fa: AwsOp[A]): GreenflyState[A] =
      fa match {
        case VpcOp(name, cidr) =>
          println(s"Provisioning Vpc($name, $cidr)")
          for {
            s <- State.inspect[StateMap, Option[VpcId]](_.get(name).map(_.asInstanceOf[VpcId]))
            id = s.getOrElse(VpcId(UUID.randomUUID().toString))
            _ <- State.modify[StateMap](_.updated(name, id))
          } yield Vpc(name, cidr, id).asInstanceOf[A]
        case SubnetOp(name, vpcId, cidr) =>
          println(s"Provisioning Subnet($name, $vpcId, $cidr)")
          for {
            s <- State.inspect[StateMap, Option[SubnetId]](_.get(name).map(_.asInstanceOf[SubnetId]))
            id = s.getOrElse(SubnetId(UUID.randomUUID().toString))
            _ <- State.modify[StateMap](_.updated(name, id))
          } yield Subnet(name, vpcId, cidr, id).asInstanceOf[A]
      }
  }
}
