public class StencilAction{
  protected int[] rgba = {255, 255, 255, 255};
  protected byte compVal = 0;
  protected byte stencilPixel = 0;
  protected int x = 0;
  protected int y = 0;
  
  //Sets the reference for the current colour  
  public void setColour(int[] colour){
    rgba = colour;
  }
  
  //Sets the value for comparisons
  public void setComparison(byte newComp){
    compVal = newComp;
  }
  
  //Takes the stencil, flips it, and returns a normalized version
  protected float returnNormalizedStencil(){
    return ((~stencilPixel) & 0xFF)*Colour.INV_255;
  }
  
  //Sets the current x and y positions
  public void setPostion(int newX, int newY){
    x = newX;
    y = newY;
  }
  
  //Sets the value of the stencil pixel
  public void setStencilPixel(byte newPixel){
    stencilPixel = newPixel;
  }
  
  
  //Function to update stencil pixel
  //Is the function that should be overridden
  //By default, it simply adjusts the brightness of each pixel based on the inverse of the stencil's current value 
  public void updateStencil(){
    float stencilNorm = returnNormalizedStencil();
    rgba[1] = (int)(rgba[1]*stencilNorm);
    rgba[2] = (int)(rgba[2]*stencilNorm);
    rgba[3] = (int)(rgba[3]*stencilNorm);
  }
  
  //Returns whatever the stencil's value is
  public byte returnStencilValue(){
    return stencilPixel;
  }
  
}
