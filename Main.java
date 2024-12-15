import java.io.*;
import java.util.*;
import java.util.Scanner;

class Reader {
  public static String read(String filename) throws IOException {
    InputStreamReader reader = new FileReader(filename); StringWriter stringWriter = new StringWriter(); char[] buffer = new char[1024]; int bytesRead; 
    while ((bytesRead = reader.read(buffer)) != -1) stringWriter.write(buffer, 0, bytesRead); return stringWriter.toString(); } }

abstract class Token { public int type = -1; public boolean equals(Token other) { return type == other.type; } }
class Identifier extends Token { public String name;  public Identifier(String id) { type = 0; name  = id; } public boolean equals(Identifier other) { return name.equals(other.name ); } }
class Literal    extends Token { public int    value; public Literal   (int    id) { type = 1; value = id; } public boolean equals(Literal    other) { return value ==    other.value ; } }
class Operator   extends Token { public int    kind;  public Operator  (int    id) { type = 2; kind  = id; } public boolean equals(Operator   other) { return kind  ==    other.kind  ; } }
class Assignment extends Token { public Assignment() { type = 3; } }        class LBrace     extends Token { public LBrace()     { type = 4; } }
class RBrace     extends Token { public RBrace()     { type = 5; } }        class LParen     extends Token { public LParen()     { type = 6; } }
class RParen     extends Token { public RParen()     { type = 7; } }        class Semicolon  extends Token { public Semicolon()  { type = 8; } }
class At         extends Token { public At()         { type = 9; } }
class Increment  extends Token { public Increment()  { type = 10; } }
class Decrement  extends Token { public Decrement()  { type = 11; } }

class Tokenizer {
  private int pointer = 0; private String string; private ArrayList<Token> tokens = new ArrayList<Token>(); 
  public Tokenizer(String input)                                                                                                                  throws IllegalArgumentException
  { string = input; tokenize(); }
  public ArrayList<Token> getTokens() { return tokens; }
  private char get() { char out = peek(); pointer++; return out; }
  private char peek() { return pointer < string.length() ? string.charAt(pointer) : null; }
  private void tokenize()                                                                                                                         throws IllegalArgumentException
  { while (pointer < string.length()) { Token token = getToken(); if (token != null) tokens.add(token); } }
  private void consume(char target)                                                                                                               throws IllegalArgumentException
  { if (peek() != target) throw new IllegalArgumentException(); get(); }
  private boolean condconsume(char target) { if (peek() == target) { get(); return true; } return false; }
  private Token getToken()                                                                                                                        throws IllegalArgumentException
  { char curr = peek(); if (" 1234567890".indexOf(curr) > 0) return matchLiteral(); if (" qwertyuiopasdfghjklzxcvbnm".indexOf(curr) > 0) return matchIdentifier(); get();
    if (curr == '\'') { if (peek() != '\\') { char value = get(); consume('\''); return new Literal((int)value); } get(); 
      int v = 0; if (peek() == 'n') v = 10; if (peek() == '\\') v = 92; if (peek() == '\'') v = 39; if (v == 0) throw new IllegalArgumentException(); get(); consume('\''); return new Literal(v); }
    if (curr == '{' ) return new LBrace(); if (curr == '}') return new RBrace(); if (curr == '(') return new LParen(); if (curr == ')') return new RParen(); if (curr == ';') return new Semicolon();
    if (curr == '+' && peek() == '+') { get(); return new Increment(); } if (curr == '-' && peek() == '-') { get(); return new Decrement(); }
    if ("    +-*/%".indexOf(curr) > 0) return new Operator("   +-*/%".indexOf(curr)); if (curr == '@') return new At(); 
    if (curr == '<' ) return new Operator(condconsume('=') ? 9 : 8); if (curr == '>') return new Operator(condconsume('=') ? 11 : 12); if (curr == '!') { consume('='); return new Operator(13); }
    if (curr == '=' ) return condconsume('=') ? new Operator(10) : new Assignment(); if (curr == '\\') { while (get() != '\\'); } return null; }
  private Token matchLiteral()    { int    num  = 0 ; while (" 1234567890"                .indexOf(peek()) > 0) num   = num * 10 + (get() - '0'); return new Literal   (num ); }
  private Token matchIdentifier() { String name = ""; while (" qwertyuiopasdfghjklzxcvbnm".indexOf(peek()) > 0) name += get();                    return new Identifier(name); } }

class Block { ArrayList<Stmt> stmts; public Block(ArrayList<Stmt> stmts) { this.stmts = stmts; } public void execute(HashMap<String, Integer> values) { for (Stmt stmt : stmts) stmt.execute(values); } }
abstract class Stmt { abstract protected void execute (HashMap<String, Integer> values); }
abstract class Expr { abstract protected int  evaluate(HashMap<String, Integer> values); }
abstract class BinaryOperator extends Expr {
  private Expr left, right; abstract protected int calc(int left, int right); public BinaryOperator(Expr left, Expr right) { this.left = left; this.right = right; }
  public int evaluate(HashMap<String, Integer> values) { return calc(left.evaluate(values), right.evaluate(values)); } }
class Add extends BinaryOperator { public Add(Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left + right; } }
class Sub extends BinaryOperator { public Sub(Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left - right; } }
class Mul extends BinaryOperator { public Mul(Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left * right; } }
class Div extends BinaryOperator { public Div(Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left / right; } }
class Mod extends BinaryOperator { public Mod(Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left % right; } }
class Lt  extends BinaryOperator { public Lt (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left <  right ? 1 : 0; } }
class Le  extends BinaryOperator { public Le (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left <= right ? 1 : 0; } }
class Gt  extends BinaryOperator { public Gt (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left >  right ? 1 : 0; } }
class Ge  extends BinaryOperator { public Ge (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left >= right ? 1 : 0; } }
class Eq  extends BinaryOperator { public Eq (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left == right ? 1 : 0; } }
class Ne  extends BinaryOperator { public Ne (Expr left, Expr right) { super(left, right); } protected int calc(int left, int right) { return left != right ? 1 : 0; } }
class Number extends Expr { private int value; public Number(int value) { this.value = value; } public int evaluate(HashMap<String, Integer> values) { return value; } }
abstract class Var extends Expr { abstract protected String getName(HashMap<String, Integer> values); }
class Variable extends Var {
  private static Scanner scanner = new Scanner(System.in); private static String buffer = ""; private static int bufferIndex = 0; private String name;
  public Variable(String name) { this.name = name; }
  public String getName(HashMap<String, Integer> values) { return name; }
  public int evaluate(HashMap<String, Integer> values)
    { if (name.equals("read")) return scanner.nextInt();
    if (name.equals("get")) { while (bufferIndex >= buffer.length()) { buffer = scanner.nextLine(); bufferIndex = 0; } int a = (int) buffer.charAt(bufferIndex++); return a != 32 ? a : evaluate(values); } return values.getOrDefault(name, 0); } }
class Reference extends Var { private Var base; private Expr ref; public Reference(Var base, Expr ref) { this.base = base; this.ref = ref; }
  public String getName(HashMap<String, Integer> values) { return base.getName(values) + " " + ref.evaluate(values); }
  public int evaluate(HashMap<String, Integer> values) { return values.getOrDefault(getName(values), 0); } }
class Equals extends Stmt { 
  private Var var; private Expr expr; public Equals(Var var, Expr expr) { this.var = var; this.expr = expr; }
  public void execute(HashMap<String, Integer> values) { int value = expr.evaluate(values); 
  if (var.getName(values).equals("write")) System.out.print(value); if (var.getName(values).equals("put")) System.out.print((char) value); values.put(var.getName(values), value); } }
class If extends Stmt {
  private Expr cond; private Block t, f; public If(Expr condition, Block t, Block f) { cond = condition; this.t = t; this.f = f; }
  public void execute(HashMap<String, Integer> values) { (cond.evaluate(values) != 0 ? t : f).execute(values); } }
class While extends Stmt {
  private Expr cond; private Block b; public While(Expr condition, Block b) { cond = condition; this.b = b; }
  public void execute(HashMap<String, Integer> values) { while (cond.evaluate(values) != 0) b.execute(values); } }

class Parser {
  private int pointer = 0; private ArrayList<Token> tokens;
  public Parser(ArrayList<Token> tokens) { this.tokens = tokens; }
  private Token get() { Token out = peek(); pointer++; return out; }
  private Token peek() { return pointer < tokens.size() ? tokens.get(pointer) : null; }
  private void consume(Token token)                                                                                                               throws IllegalArgumentException
  { if (!get().equals(token)) throw new IllegalArgumentException(); }
  public Block parse()                                                                                                                            throws IllegalArgumentException
  { Block result = parseBlock(); if (pointer < tokens.size()) throw new IllegalArgumentException(); return result; }
  private Block parseBlock()                                                                                                                      throws IllegalArgumentException
  { ArrayList<Stmt> stmts = new ArrayList<Stmt>(); while (true) { Stmt stmt = parseStmt(); if (stmt == null) return new Block(stmts); stmts.add(stmt); } }
  private Stmt parseStmt()                                                                                                                        throws IllegalArgumentException
  { Token token = peek(); if (!(token instanceof Identifier)) return null; String name = ((Identifier) token).name;
    if (name.equals("while"))
    { get(); consume(new LParen()); Expr cond = parseExpr(); consume(new RParen()); consume(new LBrace()); Block b = parseBlock(); consume(new RBrace()); return new While(cond, b); }
    if (name.equals("if")) { get(); 
      consume(new LParen()); Expr cond = parseExpr(); consume(new RParen()); consume(new LBrace()); Block l = parseBlock(); consume(new RBrace()); 
      if (!peek().equals(new Identifier("else"))) return new If(cond, l, new Block(new ArrayList<Stmt>()));
      consume(new Identifier("else")); consume(new LBrace()); Block r = parseBlock(); consume(new RBrace()); return new If(cond, l, r);
    } Var var = parseVar(); 
    if (peek() instanceof Increment) { get(); consume(new Semicolon()); return new Equals(var, new Add(var, new Number(1))); }
    if (peek() instanceof Decrement) { get(); consume(new Semicolon()); return new Equals(var, new Sub(var, new Number(1))); }
    consume(new Assignment()); Expr expr = parseExpr(); consume(new Semicolon()); return new Equals(var, expr); }
  private Expr parseExpr()                                                                                                                        throws IllegalArgumentException
  { Expr l = parseChunk(); if (peek() instanceof Operator && ((Operator) peek()).kind > 7) { int kind = ((Operator) get()).kind; Expr r = parseChunk();
    if (kind<9) return new Lt(l, r); if (kind<10) return new Le(l, r); if (kind<11) return new Eq(l, r); if (kind<12) return new Ge(l, r); if (kind<13) return new Gt(l, r); return new Ne(l,r); } return l; }
  private Expr parseChunk   ()                                                                                                                    throws IllegalArgumentException
  { Expr l = parseSubchunk   (); while (peek() instanceof Operator && ((Operator) peek()).kind < 5 && ((Operator) peek()).kind > 2) { int p = ((Operator) get()).kind; Expr r = parseSubchunk   ();
    l = p < 4 ? new Add(l, r) :                         new Sub(l, r); } return l; }
  private Expr parseSubchunk()                                                                                                                    throws IllegalArgumentException
  { Expr l = parseSubsubchunk(); while (peek() instanceof Operator && ((Operator) peek()).kind < 8 && ((Operator) peek()).kind > 4) { int p = ((Operator) get()).kind; Expr r = parseSubsubchunk();
    l = p < 6 ? new Mul(l, r) : p < 7 ? new Div(l, r) : new Mod(l, r); } return l; }
  private Expr parseSubsubchunk()                                                                                                                 throws IllegalArgumentException
  { if (peek() instanceof Identifier) return parseVar(); if (peek() instanceof Literal) return new Number(((Literal) get()).value); 
  if (get() instanceof LParen) { Expr expr = parseExpr(); consume(new RParen()); return expr; } throw new IllegalArgumentException(); }
  private Expr parseAtom()                                                                                                                        throws IllegalArgumentException
  { if (peek() instanceof Identifier) return new Variable(((Identifier) get()).name); if (peek() instanceof Literal) return new Number(((Literal) get()).value); 
  if (get() instanceof LParen) { Expr expr = parseExpr(); consume(new RParen()); return expr; } throw new IllegalArgumentException(); }
  private Var parseVar()                                                                                                                          throws IllegalArgumentException
  { Var var = new Variable(((Identifier) get()).name); while (peek() instanceof At) { get(); var = new Reference(var, parseAtom()); } return var; } }

public class Main {
  public static void main(String[] args) throws IOException, IllegalArgumentException { (new Parser((new Tokenizer(Reader.read(args[0]))).getTokens())).parse().execute(new HashMap<String, Integer>()); } }
