package com.chrisomeara.pillar

object ReplicationOptions {
  val default = new ReplicationOptions(Map("class" -> "SimpleStrategy", "replication_factor" -> 3))
}

class ReplicationOptions(options: Map[String, Any]) {
  override def toString: String = {
    "{" + options.map {
      case (key, value) =>
        value match {
          case number: Int => "'%s':%d".format(key, number)
          case string: String => "'%s':'%s'".format(key, string)
        }
    }.mkString(",") + "}"
  }
}