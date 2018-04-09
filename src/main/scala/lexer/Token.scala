package lexer

/*
Language elements:

Inspired by LOGO
http://el.media.mit.edu/logo-foundation/what_is_logo/logo_programming.html

forward 10
left 90
right 90
penup
pendown
repeat 4 [ fd 50 lt 90 ]

LATER
hideturtle
home
setx
sety
pos
xcor
ycor
heading
fd 10
lt 90
rt 90
to dashline repeat 5 [setwidth 1 fd 10 setwidth 3 fd 10] end


<block> ::= (<statement>)+

<statement> ::=  "forward"  <literal>
              | "left"  <literal>
              | "right" <literal>
              | "penup"
              | "pendown"
              | "repeat" <literal> [ <block> ]
*/


import scala.util.parsing.input.Positional

sealed trait Token extends Positional

//case class IDENTIFIER(str: String) extends Token

case class NUMBER(str: Int) extends Token

///case class LITERAL(str: String) extends Token

case object FORWARD extends Token

case object LEFT extends Token

case object RIGHT extends Token

case object PENUP extends Token

case object PENDOWN extends Token

case object REPEAT extends Token

case object LEFTBRACKET extends Token

case object RIGHTBRACKET extends Token

