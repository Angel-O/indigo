package indigo.gameengine

import scala.collection.mutable

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures", "org.wartremover.warts.NonUnitStatements"))
final class QuickCache[A](private val cache: mutable.HashMap[CacheKey, A]) {

  def fetch(key: CacheKey): Option[A] =
    cache.get(key)

  def add(key: CacheKey, value: => A): A = {
    cache.update(key, value)
    value
  }

  def fetchOrAdd(key: CacheKey, value: => A): A =
    fetch(key).getOrElse(add(key, value))

  def purgeAll(): QuickCache[A] = {
    cache.clear()
    this
  }

  def purge(key: CacheKey): QuickCache[A] = {
    cache.remove(key)
    this
  }

  def keys: List[CacheKey] =
    cache.keys.toList

  def all: List[(CacheKey, A)] =
    cache.toList

}

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
object QuickCache {

  def apply[A](key: String)(value: => A)(implicit cache: QuickCache[A]): A =
    cache.fetchOrAdd(CacheKey(key), value)

  def empty[A]: QuickCache[A] =
    new QuickCache[A](mutable.HashMap.empty[CacheKey, A])

}

final class CacheKey(val value: String) extends AnyVal
object CacheKey {
  def apply(value: String): CacheKey =
    new CacheKey(value)
}

trait ToCacheKey[A] {
  def toKey(a: A): CacheKey
}
object ToCacheKey {
  def apply[A](f: A => CacheKey): ToCacheKey[A] =
    new ToCacheKey[A] {
      def toKey(a: A): CacheKey = f(a)
    }

  implicit val s: ToCacheKey[String] =
    ToCacheKey(CacheKey.apply)

  implicit val i: ToCacheKey[Int] =
    ToCacheKey(p => CacheKey(p.toString))

}
