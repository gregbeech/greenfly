package greenfly

import cats.data._
import cats.implicits._
import cats.effect.IO
import cats.free.Free
import cats.free.Free.liftF

package object aws {
  type Env = Map[String, String]
  type Log = Vector[String]
  type StateMap = Map[String, Any]
  type InfraIO[A] = ReaderWriterStateT[IO, Env, Log, StateMap, A]

  object InfraIO {
    def pure[A](a: A): InfraIO[A] = ReaderWriterStateT.pure[IO, Env, Log, StateMap, A](a)

    def tell[A](log: Log): InfraIO[Unit] = ReaderWriterStateT.tell[IO, Env, Log, StateMap](log)

    def inspect[A](f: StateMap => A): InfraIO[A] = ReaderWriterStateT.inspect[IO, Env, Log, StateMap, A](f)
    def modify[A](f: StateMap => StateMap): InfraIO[Unit] = ReaderWriterStateT.modify[IO, Env, Log, StateMap](f)
  }

  type Aws[A] = Free[AwsOp, A]

  def vpc(name: String, cidr: Cidr): Aws[Vpc] =
    liftF(VpcOp(name, cidr))

  def subnet(name: String, vpcId: VpcId, cidr: Cidr): Aws[Subnet] =
    liftF(SubnetOp(name, vpcId, cidr))
}
