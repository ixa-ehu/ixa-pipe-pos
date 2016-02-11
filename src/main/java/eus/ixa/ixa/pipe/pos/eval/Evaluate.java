package eus.ixa.ixa.pipe.pos.eval;

public interface Evaluate {
  
  /**
   * Evaluate the model.
   */
  public void evaluate();
  
  public void detailEvaluate();
  
  public void evalError();

}
