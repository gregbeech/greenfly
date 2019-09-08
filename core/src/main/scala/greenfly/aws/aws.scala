package greenfly.aws

import cats.InjectK
import cats.free.Free

case class Cidr(unwrap: String)
case class VpcId(unwrap: String)
case class Vpc(name: String, cidr: Cidr, id: VpcId)
case class SubnetId(unwrap: String)
case class Subnet(name: String, vpcId: VpcId, cidr: Cidr, id: SubnetId)

sealed trait AwsOp[A]
case class VpcOp(name: String, cidr: Cidr) extends AwsOp[Vpc]
case class SubnetOp(name: String, vpcId: VpcId, cidr: Cidr) extends AwsOp[Subnet]

class AwsOps[F[_]](implicit I: InjectK[AwsOp, F]) {
  def vpc(name: String, cidr: Cidr): Free[F, Vpc] = Free.inject[AwsOp, F](VpcOp(name, cidr))
  def subnet(name: String, vpcId: VpcId, cidr: Cidr): Free[F, Subnet] = Free.inject[AwsOp, F](SubnetOp(name, vpcId, cidr))
}

object AwsOps {
  implicit def awsOps[F[_]](implicit I: InjectK[AwsOp, F]): AwsOps[F] = new AwsOps[F]
}
