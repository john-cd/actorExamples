//package lexer
//
//import lexer._
//
//import scala.util.parsing.combinator.RegexParsers
//
//object Lexer extends RegexParsers {
//  override def skipWhitespace = true
//
//  override val whiteSpace = "[ \t\r\f\n]+".r
//
//  def apply(code: String): Either[LexerError, List[Token]] = {
//    parse(tokens, code) match {
//      case NoSuccess(msg, next) => Left(LexerError(Location(next.pos.line, next.pos.column), msg))
//      case Success(result, next) => Right(result)
//    }
//  }
//
//  def tokens: Parser[List[Token]] = {
//    phrase(rep1(literal | identifier) ^^ { rawTokens => rawTokens })
//  }
//
//  def identifier: Parser[IDENTIFIER] = positioned {
//    "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
//  }
//
//  def literal: Parser[LITERAL] = positioned {
//    """"[^"]*"""".r ^^ { str =>
//      val content = str.substring(1, str.length - 1)
//      LITERAL(content)
//    }
//  }
//
//  def number: Parser[NUMBER] = """(0|[1-9]\d*)""".r ^^ { str => NUMBER(str.toInt) }
//
//  def forward = positioned {
//    "forward" ^^ (_ => FORWARD)
//  }
//
//  def left = positioned {
//    "left" ^^ (_ => LEFT)
//  }
//
//  def right = positioned {
//    "right" ^^ (_ => RIGHT)
//  }
//
//  def penup = positioned {
//    "penup" ^^ (_ => PENUP)
//  }
//
//  def pendown = positioned {
//    "pendown" ^^ (_ => PENDOWN)
//  }
//
//  def repeat = positioned {
//    "repeat" ^^ (_ => REPEAT)
//  }
//
//  def leftbracket = positioned {
//    "[" ^^ (_ => LEFTBRACKET)
//  }
//
//  def rightbracket = positioned {
//    "]" ^^ (_ => RIGHTBRACKET)
//  }
//
//}
