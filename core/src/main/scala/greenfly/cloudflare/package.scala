package greenfly

import java.util.UUID

import cats._
import cats.free.Free
import cats.implicits._
import greenfly.InfraIO._

package object cloudflare {
  type Cloudflare[A] = Free[CloudflareOp, A]

  val CloudflareCompiler: CloudflareOp ~> InfraIO = new (CloudflareOp ~> InfraIO) {
    override def apply[A](fa: CloudflareOp[A]): InfraIO[A] = fa match {
      case DnsRecordOp(name) =>
        for {
          _ <- tell(Vector(s"Provisioning cloudflare.DnsRecord($name)"))
          s <- inspect(_.get(name).map(DnsRecordId))
          id = s.getOrElse(DnsRecordId(UUID.randomUUID().toString))
          _ <- modify(_.updated(name, id.unwrap))
        } yield DnsRecord(name, id).asInstanceOf[A]
    }
  }
}
