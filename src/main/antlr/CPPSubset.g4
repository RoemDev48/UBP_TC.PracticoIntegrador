grammar CPPSubset;

// Regla raíz del programa
program
    : declaration* EOF
    ;

// Declaraciones globales
declaration
    : variableDeclaration
    | functionDeclaration
    | structDeclaration
    ;

structDeclaration
    : 'struct' IDENTIFIER '{' structMemberDeclaration+ '}' ';'
    ;

structMemberDeclaration
    : typeSpecifier '*'* IDENTIFIER ('[' INT_LITERAL ']')? ';'
    ;

// Declaración de variables (soporta múltiples declaradores y ahora punteros/arreglos)
variableDeclaration
    : typeSpecifier declarator (',' declarator)* ';'
    ;

declarator
    : '*'* IDENTIFIER ('[' INT_LITERAL ']')? ('=' expression)?
    ;

// Tipos de datos soportados (agregado 'bool')
typeSpecifier
    : 'int'
    | 'char'
    | 'double'
    | 'void'
    | 'bool'
    | 'string'
    | IDENTIFIER
    ;

// Declaración de funciones
functionDeclaration
    : typeSpecifier IDENTIFIER '(' parameterList? ')' compoundStatement
    ;

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : typeSpecifier '*'* IDENTIFIER
    ;

// Sentencias
statement
    : variableDeclaration                      #varDeclStmt
    | assignmentStatement                      #assignStmt
    | compoundStatement                        #blockStmt
    | selectionStatement                       #ifStmt
    | iterationStatement                       #loopStmt
    | jumpStatement                            #jumpStmt
    | expressionStatement                      #exprStmt
    ;

// L-Values válidos para asignación: variable simple (x), arreglo (arr[i]) o puntero desreferenciado (*p)
assignmentStatement
    : lvalue '=' expression ';'
    ;

lvalue
    : unaryExpr
    ;

compoundStatement
    : '{' statement* '}'
    ;

selectionStatement
    : 'if' '(' expression ')' statement ('else' statement)?
    ;

iterationStatement
    : 'while' '(' expression ')' statement                                                  #whileLoop
    | 'for' '(' (initVar=variableDeclaration | initExpr=expressionStatement) cond=expression? ';' step=expression? ')' statement #forLoop
    ;

expressionStatement
    : expression? ';'
    ;

jumpStatement
    : 'break' ';'                              #breakStmt
    | 'continue' ';'                           #continueStmt
    | 'return' expression? ';'                 #returnStmt
    ;

// Expresiones con precedencia bien definida
expression
    : assignmentExpr
    ;

assignmentExpr
    : lvalue '=' expression                    #assignment
    | logicalOrExpr                            #exprOr
    ;

logicalOrExpr
    : logicalAndExpr ('||' logicalAndExpr)*
    ;

logicalAndExpr
    : equalityExpr ('&&' equalityExpr)*
    ;

equalityExpr
    : relationalExpr (op=('==' | '!=') relationalExpr)*
    ;

relationalExpr
    : additiveExpr (op=('<' | '>' | '<=' | '>=') additiveExpr)*
    ;

additiveExpr
    : multiplicativeExpr (op=('+' | '-') multiplicativeExpr)*
    ;

multiplicativeExpr
    : unaryExpr (op=('*' | '/' | '%') unaryExpr)*
    ;

unaryExpr
    : op=('++' | '--' | '+' | '-' | '!' | '&' | '*') unaryExpr           #unaryOp
    | primaryExpr op=('++' | '--')                                       #postfixOp
    | primaryExpr                                                        #primary
    ;

primaryExpr
    : primaryExpr '.' IDENTIFIER                                             #memberAccess
    | IDENTIFIER '[' expression ']'                                          #arrayAccess
    | IDENTIFIER                                                             #id
    | literal                                                                #lit
    | functionCall                                                           #funcCall
    | '(' expression ')'                                                     #parenthesized
    ;

functionCall
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : expression (',' expression)*
    ;

literal
    : INT_LITERAL                              #intLit
    | DOUBLE_LITERAL                           #doubleLit
    | CHAR_LITERAL                             #charLit
    | BOOLEAN_LITERAL                          #boolLit
    | STRING_LITERAL                           #stringLit
    ;

// --- REGLAS LÉXICAS ---

// Tipos de literales
INT_LITERAL : [0-9]+ ;
DOUBLE_LITERAL : [0-9]+ '.' [0-9]+ ;
CHAR_LITERAL : '\'' ( ~['\\\r\n] | '\\' [nrt'\\0] ) '\'' ;
BOOLEAN_LITERAL : 'true' | 'false' ;
STRING_LITERAL : '"' ( ~["\\\r\n] | '\\' [btnfr"'\\] )* '"' ;

// Identificadores
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]* ;

// Comentarios y espacios en blanco
WS : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
