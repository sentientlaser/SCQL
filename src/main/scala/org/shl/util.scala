package org.shl

package object util {

  import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter, Closeable}

  implicit class RichCloseable[T <: Closeable](val c:T) extends AnyVal{
    def -> [V] (function: T => V) = {
      try { function(c) } finally { c.close }
    }
  }

  implicit class RichFile(val file:File) extends AnyVal{

    def write(s:String) = {
      new BufferedWriter(new FileWriter(file)) -> {bw =>
        bw.write(s)
      }
    }

    def read() = {
      new BufferedReader(new FileReader(file)) -> {br =>
        val sb = new StringBuffer
        var l = br.readLine()
        while (l != null){
          sb.append(l)
        }
        sb.toString
      }
    }

    def ifExists[V](function:File => V) = if (file.exists()) function(file)

    def ifNotExists[V](function:File => V) = if (!file.exists()) function(file)

  }


  def toStringF[T](a:T) = a.toString

  implicit class RegexStringContext(private val sc: StringContext) {
    def regex(args:Any *) = sc.s(args).r
    def regexi(args:Any *) = ("""(?i)\Q""" + sc.s(args) + """\E""").r()
  }

  trait UnionType {
    // Credit to Miles Sabin for this little bit of awesome.
    // https://milessabin.com/blog/2011/06/09/scala-union-types-curry-howard/

    def unexpected: Nothing = sys.error("Unexpected invocation")

    private type ¬[A] = A => Nothing
    private type ∨[T, U] = ¬[¬[T] with ¬[U]]
    private type ¬¬[A] = ¬[¬[A]]
    type |[T, U] = {type union[X] = ¬¬[X] <:< (T ∨ U)}
  }
}

package util {

  object NestedObjectReflector {
    import scala.reflect.runtime.universe

    val mirror = universe.runtimeMirror(getClass.getClassLoader)
  }

  trait NestedObjectReflector {

    import scala.reflect.runtime.universe.typeOf
    import scala.reflect.runtime.universe.TypeTag

    protected final def mirror$$ = NestedObjectReflector.mirror

    protected final def self$$ = mirror$$.reflect(this).symbol.typeSignature

    protected final def modules$$[T: TypeTag]: Set[T] = {
      self$$
        .members
        .filter(_.isModule)
        .filter(_.typeSignature <:< typeOf[T])
        .map(_.asModule)
        .map(mirror$$.reflectModule(_))
        .map(_.instance.asInstanceOf[T])
        .toSet
    }
  }

  trait VerifiesOnConstruction {
    def verify: Boolean

    verify
  }
}