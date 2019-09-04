package greenfly

import cats.free.Free
import cats.free.Free.liftF

package object aws {
  type Aws[A] = Free[AwsOp, A]

  def vpc(name: String, cidr: Cidr): Aws[Vpc] =
    liftF(VpcOp(name, cidr))

  def subnet(name: String, vpcId: VpcId, cidr: Cidr): Aws[Subnet] =
    liftF(SubnetOp(name, vpcId, cidr))
}
