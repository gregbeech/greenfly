package greenfly.datadog

import cats.InjectK
import cats.free.Free

case class DashboardId(unwrap: String)
case class Dashboard(name: String, id: DashboardId)

sealed trait DatadogOp[A]
case class DashboardOp(name: String) extends DatadogOp[Dashboard]

class DatadogOps[F[_]](implicit I: InjectK[DatadogOp, F]) {
  def dashboard(name: String): Free[F, Dashboard] = Free.inject[DatadogOp, F](DashboardOp(name))
}

object DatadogOps {
  implicit def datadogK[F[_]](implicit I: InjectK[DatadogOp, F]): DatadogOps[F] = new DatadogOps[F]
}
