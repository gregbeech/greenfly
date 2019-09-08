import cats.data._
import cats.implicits._
import cats.effect.IO

package object greenfly {
  type Env = Map[String, String]
  type Log = Vector[String]
  type StateMap = Map[String, String]
  type InfraIO[A] = ReaderWriterStateT[IO, Env, Log, StateMap, A]

  object InfraIO {
    def pure[A](a: A): InfraIO[A] = ReaderWriterStateT.pure[IO, Env, Log, StateMap, A](a)
    def liftF[A](fa: IO[A]): InfraIO[A] = ReaderWriterStateT.liftF(fa)

    def tell[A](log: Log): InfraIO[Unit] = ReaderWriterStateT.tell[IO, Env, Log, StateMap](log)

    def inspect[A](f: StateMap => A): InfraIO[A] = ReaderWriterStateT.inspect[IO, Env, Log, StateMap, A](f)
    def modify[A](f: StateMap => StateMap): InfraIO[Unit] = ReaderWriterStateT.modify[IO, Env, Log, StateMap](f)
  }
}
