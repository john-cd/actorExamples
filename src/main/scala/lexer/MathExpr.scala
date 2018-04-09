//package mathexpr
//
//
//import scala.util.parsing.combinator.JavaTokenParsers
//
//
//sealed abstract class Tree
//
//case class Sum(l: Tree, r: Tree) extends Tree {
//  override def toString(): String =
//    "(" + l.toString() + "+" + r.toString() + ")"
//}
//
//case class Sub(t1: Tree, t2: Tree) extends Tree
//
//case class Var(n: String) extends Tree {
//  override def toString() = n
//}
//
//case class Const(v: Double) extends Tree {
//  override def toString() = v.toString
//}
//
//case class Power(x: Var, y: Const) extends Tree {
//  override def toString() = x + "^" + y
//}
//
//case class Product(x: Tree, y: Tree) extends Tree {
//  override def toString() = x + "*" + y
//
//  case class Div(t1: Tree, t2: Tree) extends Tree
//
//}
//
//object Math {
//  type Environment = String => Double
//
//  def eval(t: Tree, env: Environment): Double = t match {
//    case Sum(l, r) => eval(l, env) + eval(r, env)
//    case Product(l, r) => eval(l, env) * eval(r, env)
//    case Power(x, y) => java.lang.Math.pow(eval(x, env), eval(y, env))
//    case Var(n) => env(n)
//    case Const(v) => v
//    case Sub(t1, t2) => eval(t1, env) - eval(t2, env)
//    case Div(t1, t2) => eval(t1, env) / eval(t2, env)
//  }
//
//  def simplify(t: Tree): Tree = t match {
//    case Power(l, Const(y)) if (y == 1) => l
//    case Product(l, r) if (r == Const(1)) => simplify(l)
//    case Product(l, r) if (l == Const(1)) => simplify(r)
//    case Product(l, r) => Product(simplify(l), simplify(r))
//    case Sum(Const(a), Const(b)) => Const(a + b)
//    case Sum(l, r) if (l == r) => Product(Const(2), l)
//    case Sum(l, r) => Sum(simplify(l), simplify(r))
//    //	case Sub(t1, t2) if t2 == 0 =>
//    //    case Div(t1, t2) if t2 == 1  =>
//    case _ => t
//  }
//
//  def derive(t: Tree, v: String): Tree = t match {
//    case Power(Var(n), Const(y)) if (v == n) => Product(Const(y),
//      Power(Var(n), Const(y - 1)))
//    case Sum(l, r) => Sum(derive(l, v), derive(r, v))
//    case Var(n) if (v == n) => Const(1)
//    //	case Sub(t1, t2) => Sub(derive(l, v), derive(r, v))
//    //    case Div(t1, t2) =>
//    case _ => Const(0)
//  }
//
//
//  trait ExprParsers1 extends JavaTokenParsers {
//
//    lazy val expr: Parser[Any] =
//      term ~ rep("[+-]".r ~ term)
//
//    lazy val term: Parser[Any] =
//      factor ~ rep("[*/]".r ~ factor)
//
//    lazy val factor: Parser[Any] =
//      "(" ~> expr <~ ")" | floatingPointNumber
//
//  }
//
//  trait ExprParsers2 extends JavaTokenParsers {
//
//    lazy val expr: Parser[Tree] = term ~ rep("[+-]".r ~ term) ^^ {
//      case t ~ ts => ts.foldLeft(t) {
//        case (t1, "+" ~ t2) => Sum(t1, t2)
//        case (t1, "-" ~ t2) => Sub(t1, t2)
//      }
//    }
//
//    lazy val term = factor ~ rep("[*/]".r ~ factor) ^^ {
//      case t ~ ts => ts.foldLeft(t) {
//        case (t1, "*" ~ t2) => Product(t1, t2)
//        case (t1, "/" ~ t2) => Div(t1, t2)
//      }
//    }
//
//    lazy val factor = "(" ~> expr <~ ")" | num
//
//    lazy val num = floatingPointNumber ^^ { t => Num(t.toDouble) }
//  }
//
//
//}
