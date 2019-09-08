package greenfly

import java.util.UUID

import cats._
import cats.free.Free
import cats.implicits._
import greenfly.InfraIO._

package object aws {
  type Aws[A] = Free[AwsOp, A]

  val AwsCompiler: AwsOp ~> InfraIO = new (AwsOp ~> InfraIO) {
    override def apply[A](fa: AwsOp[A]): InfraIO[A] = fa match {
      case VpcOp(name, cidr) =>
        for {
          _ <- tell(Vector(s"Provisioning aws.Vpc($name, $cidr)"))
          s <- inspect(_.get(name).map(VpcId))
          id = s.getOrElse(VpcId(UUID.randomUUID().toString))
          _ <- modify(_.updated(name, id.unwrap))
        } yield Vpc(name, cidr, id).asInstanceOf[A]
      case SubnetOp(name, vpcId, cidr) =>
        for {
          _ <- tell(Vector(s"Provisioning aws.Subnet($name, $vpcId, $cidr)"))
          s <- inspect(_.get(name).map(SubnetId))
          id = s.getOrElse(SubnetId(UUID.randomUUID().toString))
          _ <- modify(_.updated(name, id.unwrap))
        } yield Subnet(name, vpcId, cidr, id)
    }
  }
}
