package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;

public interface RegexNode {
  public String toString ();
  public int descendants ();  
  public void toBinary();
  public int collectUnknown();
  public void setNullable();
  public BoolExpr isNullable();
  public void setLen();
  public int getLen();
  public void calUpto(int upto);
  public void setPairs();
  public BoolExpr[][] getPairs();
  public String finalString();
  public void setEpsilon();
  public boolean getEpsilon();
}
