package greenfly.cloudflare

import cats.InjectK
import cats.free.Free

case class DnsRecordId(unwrap: String)
case class DnsRecord(name: String, id: DnsRecordId)

sealed trait CloudflareOp[A]
case class DnsRecordOp(name: String) extends CloudflareOp[DnsRecord]

class CloudflareOps[F[_]](implicit I: InjectK[CloudflareOp, F]) {
  def dnsRecord(name: String): Free[F, DnsRecord] = Free.inject[CloudflareOp, F](DnsRecordOp(name))
}

object CloudflareOps {
  implicit def cloudflareOps[F[_]](implicit I: InjectK[CloudflareOp, F]): CloudflareOps[F] = new CloudflareOps[F]
}
