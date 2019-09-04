package greenfly.aws

import java.util.UUID

import cats.implicits._

sealed trait AwsOp[A] {
  def compile: InfraIO[A] // Probably shouldn't be on here
}

case class VpcOp(name: String, cidr: Cidr) extends AwsOp[Vpc] {
  def compile: InfraIO[Vpc] = for {
    _ <- InfraIO.tell(Vector(s"Provisioning Vpc($name, $cidr)"))
    s <- InfraIO.inspect(_.get(name).map(_.asInstanceOf[VpcId]))
    id = s.getOrElse(VpcId(UUID.randomUUID().toString))
    _ <- InfraIO.modify(_.updated(name, id))
  } yield Vpc(name, cidr, id)
}

case class SubnetOp(name: String, vpcId: VpcId, cidr: Cidr) extends AwsOp[Subnet] {
  def compile: InfraIO[Subnet] = for {
    _ <- InfraIO.tell(Vector(s"Provisioning Subnet($name, $vpcId, $cidr)"))
    s <- InfraIO.inspect(_.get(name).map(_.asInstanceOf[SubnetId]))
    id = s.getOrElse(SubnetId(UUID.randomUUID().toString))
    _ <- InfraIO.modify(_.updated(name, id))
  } yield Subnet(name, vpcId, cidr, id)
}

case class Cidr(unwrap: String)

case class VpcId(unwrap: String)
case class Vpc(name: String, cidr: Cidr, id: VpcId)

case class SubnetId(unwrap: String)
case class Subnet(name: String, vpcId: VpcId, cidr: Cidr, id: SubnetId)
