//package lexer
//
//import scala.util.parsing.combinator.RegexParsers
//
//// https://enear.github.io/2016/03/31/parser-combinators/
//// https://github.com/enear/parser-combinators-tutorial
//
//sealed trait WorkflowToken
//
//case class IDENTIFIER(str: String) extends WorkflowToken
//
//case class LITERAL(str: String) extends WorkflowToken
//
//case class INDENTATION(spaces: Int) extends WorkflowToken
//
//case object EXIT extends WorkflowToken
//
//case object READINPUT extends WorkflowToken
//
//case object CALLSERVICE extends WorkflowToken
//
//case object SWITCH extends WorkflowToken
//
//case object OTHERWISE extends WorkflowToken
//
//case object COLON extends WorkflowToken
//
//case object ARROW extends WorkflowToken
//
//case object EQUALS extends WorkflowToken
//
//case object COMMA extends WorkflowToken
//
//case object INDENT extends WorkflowToken
//
//case object DEDENT extends WorkflowToken
//
//
//trait WorkflowCompilationError
//
//case class WorkflowLexerError(msg: String) extends WorkflowCompilationError
//
//case class WorkflowParserError(msg: String) extends WorkflowCompilationError
//
//
//object WorkflowLexer extends RegexParsers {
//
//  override def skipWhitespace = true
//
//  override val whiteSpace = "[ \t\r\f]+".r
//
//  def identifier: Parser[IDENTIFIER] = {
//    "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
//  }
//
//  def literal: Parser[LITERAL] = {
//    """"[^"]*"""".r ^^ { str =>
//      val content = str.substring(1, str.length - 1)
//      LITERAL(content)
//    }
//  }
//
//  def indentation: Parser[INDENTATION] = {
//    "\n[ ]*".r ^^ { whitespace =>
//      val nSpaces = whitespace.length - 1
//      INDENTATION(nSpaces)
//    }
//  }
//
//  def exit = "exit" ^^ (_ => EXIT)
//
//  def readInput = "read input" ^^ (_ => READINPUT)
//
//  def callService = "call service" ^^ (_ => CALLSERVICE)
//
//  def switch = "switch" ^^ (_ => SWITCH)
//
//  def otherwise = "otherwise" ^^ (_ => OTHERWISE)
//
//  def colon = ":" ^^ (_ => COLON)
//
//  def arrow = "->" ^^ (_ => ARROW)
//
//  def equals = "==" ^^ (_ => EQUALS)
//
//  def comma = "," ^^ (_ => COMMA)
//
//
//  // rep1 recognizes one or more repetitions of its argument
//  // phrase attempts to consume all input until no more is left.
//
//  def tokens: Parser[List[WorkflowToken]] = {
//    phrase(rep1(exit | readInput | callService | switch | otherwise | colon | arrow
//      | equals | comma | literal | identifier | indentation)) ^^ { rawTokens =>
//      processIndentations(rawTokens)
//    }
//  }
//
//  private def processIndentations(tokens: List[WorkflowToken],
//                                  indents: List[Int] = List(0)): List[WorkflowToken] = {
//    tokens.headOption match {
//
//      // if there is an increase in indentation level, we push this new level into the stack
//      // and produce an INDENT
//      case Some(INDENTATION(spaces)) if spaces > indents.head =>
//        INDENT :: processIndentations(tokens.tail, spaces :: indents)
//
//      // if there is a decrease, we pop from the stack until we have matched the new level,
//      // producing a DEDENT for each pop
//      case Some(INDENTATION(spaces)) if spaces < indents.head =>
//        val (dropped, kept) = indents.partition(_ > spaces)
//        (dropped map (_ => DEDENT)) ::: processIndentations(tokens.tail, kept)
//
//      // if the indentation level stays unchanged, no tokens are produced
//      case Some(INDENTATION(spaces)) if spaces == indents.head =>
//        processIndentations(tokens.tail, indents)
//
//      // other tokens are ignored
//      case Some(token) =>
//        token :: processIndentations(tokens.tail, indents)
//
//      // the final step is to produce a DEDENT for each indentation level still remaining, thus
//      // "closing" the remaining open INDENTS
//      case None =>
//        indents.filter(_ > 0).map(_ => DEDENT)
//
//    }
//  }
//
//  def apply(code: String): Either[WorkflowLexerError, List[WorkflowToken]] = {
//    parse(tokens, code) match {
//      case NoSuccess(msg, next) => Left(WorkflowLexerError(msg))
//      case Success(result, next) => Right(result)
//    }
//  }
//
//}