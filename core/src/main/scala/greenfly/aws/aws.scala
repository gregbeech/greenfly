package greenfly.aws

sealed trait AwsOp[A]
case class VpcOp(name: String, cidr: Cidr) extends AwsOp[Vpc]
case class SubnetOp(name: String, vpcId: VpcId, cidr: Cidr) extends AwsOp[Subnet]

case class Cidr(unwrap: String)

case class VpcId(unwrap: String)
case class Vpc(name: String, cidr: Cidr, id: VpcId)

case class SubnetId(unwrap: String)
case class Subnet(name: String, vpcId: VpcId, cidr: Cidr, id: SubnetId)
