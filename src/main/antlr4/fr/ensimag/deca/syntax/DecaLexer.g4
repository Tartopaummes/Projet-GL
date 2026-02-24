lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.
PRINTLN:'println';
PRINT:'print';
PRINTX:'printx';
PRINTLNX:'printlnx';
ASM:'asm';
CLASS:'class';
EXTENDS:'extends';
ELSE:'else';
FALSE:'false';
IF:'if';
INSTANCEOF:'instanceof';
NEW :'new';
NULL :'null';
READINT:'readInt';
READFLOAT :'readFloat';
PROTECTED : 'protected';
RETURN : 'return';
THIS :'this';
TRUE : 'true';
WHILE : 'while';

LT:'<';
GT:'>';
EQUALS:'=';
PLUS:'+';
MINUS:'-' ;
TIMES:'*';
SLASH: '/';
PERCENT:'%';
DOT:'.';
COMMA :',';
SEMI:';';
EXCLAM:'!';
EQEQ:'==';
NEQ:'!=';
GEQ:'>=';
LEQ:'<=';
AND: '&&' ;
OR:'||';
OBRACE:'{';
CBRACE:'}';
OPARENT : '(';
CPARENT : ')';

//fragments are sub-rules that are used in other rules do not designate Deca lexical units.
fragment DIGIT:'0'..'9';
fragment POSITIVE_DIGIT:'1'..'9';
fragment NUM : DIGIT+;
fragment SIGN : ('+' | '-' )?;
fragment EXP : ('E' | 'e') (SIGN)? NUM;
fragment DEC : NUM '.' NUM;
fragment FLOATDEC : (DEC | DEC EXP) ('F' | 'f'  )?;
fragment DIGITHEX : '0'..'9' | 'A'..'F' | 'a'..'f';
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') (SIGN)? NUM ('F' | 'f' )?;
FLOAT : FLOATDEC | FLOATHEX;
INT: '0'|(POSITIVE_DIGIT DIGIT*);
fragment LETTER:'a'..'z'|'A'..'Z';
IDENT : (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')* ;






STRING:'"' (('\\"' | '\\\\' | STRING_CAR)*) '"'
{
    String multiLineString = getText();
    //remove first and last "
    multiLineString  = multiLineString.substring(1);
    multiLineString  = multiLineString.substring(0, multiLineString.length() - 1);
    //Manage escape car \
    multiLineString  = multiLineString.replace("\\\"", "\"");
    multiLineString  = multiLineString.replace("\\\\", "\\");
    setText(multiLineString);
};
MULTI_LINE_STRING: '"' (('\\n' |'\\"' | '\\\\' | STRING_CAR )*) '"'
{
    String multiLineString = getText();
    //remove first and last "
    multiLineString  = multiLineString.substring(1);
    multiLineString  = multiLineString.substring(0, multiLineString.length() - 1);
    //Manage escape car \
    multiLineString  = multiLineString.replace("\\\"", "\"");
    multiLineString  = multiLineString.replace("\\\\", "\\");
    setText(multiLineString);
};
fragment STRING_CAR: ~('"' | '\\'  | '\n');
COMMENT :  (('//' .*? '\n') | ('/*' .*? '*/')){skip();};

// Ignore spaces, tabs, newlines and whitespaces
WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {
              skip(); // avoid producing a token
          }
    ;

fragment FILENAME :(LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE : ('#include' (' ')* '"' FILENAME '"'){doInclude(getText());};
