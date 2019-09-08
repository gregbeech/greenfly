package greenfly

import java.util.UUID

import cats._
import cats.free.Free
import cats.implicits._
import greenfly.InfraIO._

package object datadog {
  type Datadog[A] = Free[DatadogOp, A]

  val DatadogCompiler: DatadogOp ~> InfraIO = new (DatadogOp ~> InfraIO) {
    override def apply[A](fa: DatadogOp[A]): InfraIO[A] = fa match {
      case DashboardOp(name) =>
        for {
          _ <- tell(Vector(s"Provisioning datadog.Dashboard($name)"))
          s <- inspect(_.get(name).map(DashboardId))
          id = s.getOrElse(DashboardId(UUID.randomUUID().toString))
          _ <- modify(_.updated(name, id.unwrap))
        } yield Dashboard(name, id).asInstanceOf[A]
    }
  }
}
