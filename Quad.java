public class Quad{
  public static final int[][] TRI_INDICES = {{0, 2, 3}, {0, 1, 2}, {0, 1, 3}, {1, 2, 3}};
  public static final float[][] UV_COORDS = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
  private BillboardImg image;
  //r = replace pixels and do not draw areas where specified by billboard image
  //m = multiply pixels and do not draw areas where specified by billboard image
  //k = replace pixels; areas that are not to be replaced will be fill colour
  //u = multiply pixels; areas that are not to be replaced will be fill colour
  private char mode = 'r';
  private float[][] vertices = new float[4][3];
  private float[][] vertexBrightness = new float[4][4];
  private int fill = 0;
  private int stroke = 0;
  private byte flags = 0; //0 = has stroke, 1 = has fill, 2 = Removal enable, 3 = depth Write, 4 = equal transparencies
  private float maxFizzel = 1;
  private float fizzelThreshold = 1.1f;
  private StencilAction stencil = new StencilAction();
  
  public Quad(){
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = 0;
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    
    flags = 18;
    fill = 0xFFFFFFFF;
    stroke = 0xFF000000;
    stencil = new StencilAction();
  }
  
  public Quad(float[][] newVerts, int newFill, int newStroke, boolean hasFill, boolean hasStroke){
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    fill(newFill);
    stroke(newStroke);
    flags = 16;
    if(hasFill)
      flags|=2;
    if(hasStroke)
      flags|=1;
    stencil = new StencilAction();
  }
  public Quad(float[][] newVerts, BillboardImg img){
    flags = 18;
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    fill = 0xFFFFFFFF;
    stroke = 0xFF000000;
    image = img;
    stencil = new StencilAction();
  }
  public Quad(float[][] newVerts, BillboardImg img, int newFill, int newStroke, boolean hasFill, boolean hasStroke){
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    fill(newFill);
    stroke(newStroke);
    flags = 16;
    if(hasFill)
      flags|=2;
    if(hasStroke)
      flags|=1;
    image = img;
    stencil = new StencilAction();
  }

  public void setImage(BillboardImg img){
    image = img;
  }
  
  public void setStencilAction(StencilAction newAction){
    stencil = newAction;
  }
  
  public StencilAction returnStencilActionPtr(){
    return stencil;
  }
  
  //Setting the rect's vertices
  public void setVertices(float[][] newVertices){
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVertices[i][j];
      }
    }
  }
  public void setFizzel(float newMax, float newThreshold){
    maxFizzel = newMax;
    fizzelThreshold = newThreshold;
  }
  public float returnMaxFizzel(){
    return maxFizzel;
  }
  public float returnFizzelThreshold(){
    return fizzelThreshold;
  } 
  
  //Setting the fill colour of the rect
  public void fill(short r, short g, short b){
    flags|=2;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    fill = 0xFF000000|(r << 16)|(g << 8)|b;
  }
  public void fill(short r, short g, short b, short a){
    flags|=2;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    a&=0xFF;
    fill = (a << 24)|(r << 16)|(g << 8)|b;
  }
  public void fill(int colour){
    flags|=2;
    if((colour >>> 24) == 0){
      if(colour <= 0xFF)
          colour = 0xFF000000 | (colour << 16) | (colour << 8) | colour;
      else if(colour <= 0xFFFF)
          colour = ((colour & 0xFF00) << 16) | ((colour & 0xFF) << 16) | ((colour & 0xFF) << 8) | (colour & 0xFF);
      else
          colour = 0xFF000000 | colour;
    }
    fill = colour;
  }
  public void fill(int colour, short alpha){
    flags|=2;
    alpha&=0xFF;
    colour = (colour & 0xFFFFFF);
    if(colour > 0xFF)
      fill = (alpha << 24)|colour;
    else
      fill = (alpha << 24)|(colour << 16)|(colour << 8)|colour;
  }
  
  //Setting the stroke colour of the rect
  public void stroke(short r, short g, short b){
    flags|=1;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    stroke = 0xFF000000|(r << 16)|(g << 8)|b;
  }
  public void stroke(short r, short g, short b, short a){
    flags|=1;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    a&=0xFF;
    stroke = (a << 24)|(r << 16)|(g << 8)|b;
  }
  public void stroke(int colour){
    flags|=1;
    if((colour >>> 24) == 0){
      if(colour <= 0xFF)
          colour = 0xFF000000 | (colour << 16) | (colour << 8) | colour;
      else if(colour <= 0xFFFF)
          colour = ((colour & 0xFF00) << 16) | ((colour & 0xFF) << 16) | ((colour & 0xFF) << 8) | (colour & 0xFF);
      else
          colour = 0xFF000000 | colour;
    }
    stroke = colour;
  }
  public void stroke(int colour, short alpha){
    flags|=1;
    alpha&=0xFF;
    colour = (colour & 0xFFFFFF);
   if(colour > 0xFF)
      stroke = (alpha << 24)|colour;
    else
      stroke = (alpha << 24)|(colour << 16)|(colour << 8)|colour;
  }
  public void setMode(char newMode){
    switch(newMode){
      case 'r':
      case 'm':
      case 'k':
      case 'u':
        mode = newMode;
        break;
      default:
        mode = 'r';
    }
  }
  
  public void setRemoval(boolean removalEnable){
    if(removalEnable)
      flags|=4;
    else
      flags&=-5;
  }

  //Selecting having no fill
  public void noFill(){
    flags&=-3;
  }
  //Selecting having no stroke
  public void noStroke(){
    flags&=-2;
  }
  
  //Should disable depth write if noDraw in billboards is enabled and enable depth write if noDraw is disabled
  public void setDepthWrite(boolean depthWrite){
    if(depthWrite)
      flags|=8;
    else
      flags&=-9;
  }
  
  //Returns if the rect has its fill enabled
  public boolean hasFill(){
    return (flags & 2) == 2;
  }
  //Returns if the rect has its stroke enabled
  public boolean hasStroke(){
    return (flags & 1) == 1;
  }
  public boolean hasRemoval(){
    return (flags & 4) == 4;
  }
  public boolean getHasDepthWrite(){
    return (flags & 8) == 8;
  }
  
  public boolean equalTransparencies(){
    return (flags & 16) == 16;
  }
  
  //Returns the fill of the rect
  public int returnFill(){
    return fill;
  }
  //Returns the stroke of the rect
  public int returnStroke(){
    return stroke;
  }
  public char returnMode(){
    return mode;
  }
  public int[] returnPixels(){
    return image.returnPixels();
  }
  public boolean hasImage(){
    return image != null;
  }
  public int returnInvisColour(){
    return image.returnInvisColour((byte)0);
  }
  public boolean shouldDrawPixel(int pixelIndex){
    return image.shouldDrawPixel(pixelIndex);
  }
  public int returnImageWidth(){
    return image.returnWidth();
  }
  public int returnImageHeight(){
    return image.returnHeight();
  }
  //Returns a specific vertex position
  public float returnVertexPosition(byte vertex, byte axis){
     if(vertex >= 0 && vertex < 3 && axis >= 0 && axis < 3)
       return vertices[vertex][axis];
     else{
       System.out.println("ERROR: VERTEX DOES NOT EXIST");
       System.exit(-1);
       return Float.intBitsToFloat(-1);
     }
  }
  //Returns the reference to all vertices
  public float[][] returnVertices(){
    float[][] returnVerts = {{vertices[0][0], vertices[0][1], vertices[0][2]},
                             {vertices[1][0], vertices[1][1], vertices[1][2]},
                             {vertices[2][0], vertices[2][1], vertices[2][2]},
                             {vertices[3][0], vertices[3][1], vertices[3][2]}};
    return returnVerts; 
  }
  
  //Returns centre position
  public float getAverageX(){
    return (vertices[0][0]+vertices[1][0]+vertices[2][0]+vertices[3][0])*0.25f; 
  }
  
  public float getAverageY(){
    return (vertices[0][1]+vertices[1][1]+vertices[2][1]+vertices[3][1])*0.25f;
  }
  
  public float getAverageZ(){
    return (vertices[0][2]+vertices[1][2]+vertices[2][2]+vertices[3][2])*0.25f;
  }

  public float[] getCentroid(){
    float[] centroid = {(vertices[0][0]+vertices[1][0]+vertices[2][0]+vertices[3][0])*0.25f,
                        (vertices[0][1]+vertices[1][1]+vertices[2][1]+vertices[3][1])*0.25f,
                        (vertices[0][2]+vertices[1][2]+vertices[2][2]+vertices[3][2])*0.25f};
    return centroid;
  }
  
  public void setVertexBrightness(float r, float g, float b, byte index){
    vertexBrightness[index][0] = 1;
    vertexBrightness[index][1] = r;
    vertexBrightness[index][2] = g;
    vertexBrightness[index][3] = b;
    float[] transparency = {Math.abs(vertexBrightness[0][0]-vertexBrightness[1][0]), 
                            Math.abs(vertexBrightness[1][0]-vertexBrightness[2][0]),
                            Math.abs(vertexBrightness[2][0]-vertexBrightness[3][0]),
                            Math.abs(vertexBrightness[3][0]-vertexBrightness[0][0])};
    if(transparency[0] <= 0.0001 && transparency[1] <= 0.0001 && transparency[2] <= 0.0001 && transparency[3] <= 0.0001)
      flags|=16;
    else
      flags&=-17;
  }
  
  public void setVertexBrightness(float a, float r, float g, float b, byte index){
    vertexBrightness[index][0] = a;
    vertexBrightness[index][1] = r;
    vertexBrightness[index][2] = g;
    vertexBrightness[index][3] = b;
    float[] transparency = {Math.abs(vertexBrightness[0][0]-vertexBrightness[1][0]), 
                            Math.abs(vertexBrightness[1][0]-vertexBrightness[2][0]),
                            Math.abs(vertexBrightness[2][0]-vertexBrightness[3][0]),
                            Math.abs(vertexBrightness[3][0]-vertexBrightness[0][0])};
    if(transparency[0] <= 0.0001 && transparency[1] <= 0.0001 && transparency[2] <= 0.0001 && transparency[3] <= 0.0001)
      flags|=16;
    else
      flags&=-17;
    
  }
  
  public void setVertexBrightness(float[] brightnessLevels, byte index){
    if(brightnessLevels.length >= 4){
      vertexBrightness[index][0] = brightnessLevels[0];
      vertexBrightness[index][1] = brightnessLevels[1];
      vertexBrightness[index][2] = brightnessLevels[2];
      vertexBrightness[index][3] = brightnessLevels[3];
    }
    else{
      vertexBrightness[index][0] = 1;
      vertexBrightness[index][1] = brightnessLevels[0];
      vertexBrightness[index][2] = brightnessLevels[1];
      vertexBrightness[index][3] = brightnessLevels[2];
    }
    float[] transparency = {Math.abs(vertexBrightness[0][0]-vertexBrightness[1][0]), 
                            Math.abs(vertexBrightness[1][0]-vertexBrightness[2][0]),
                            Math.abs(vertexBrightness[2][0]-vertexBrightness[3][0]),
                            Math.abs(vertexBrightness[3][0]-vertexBrightness[0][0])};
    if(transparency[0] <= 0.0001 && transparency[1] <= 0.0001 && transparency[2] <= 0.0001 && transparency[3] <= 0.0001)
      flags|=16;
    else
      flags&=-17;
  }

  public void setVertexBrightness(float[][] brightnessLevels){
    byte start = 0;
    flags|=16;
    if(brightnessLevels[0].length >= 4){
      start = 1;
      vertexBrightness[0][0] = brightnessLevels[0][0];
      vertexBrightness[1][0] = brightnessLevels[1][0];
      vertexBrightness[2][0] = brightnessLevels[2][0];
      vertexBrightness[3][0] = brightnessLevels[3][0];
      float[] transparency = {Math.abs(vertexBrightness[0][0]-vertexBrightness[1][0]), 
                              Math.abs(vertexBrightness[1][0]-vertexBrightness[2][0]),
                              Math.abs(vertexBrightness[2][0]-vertexBrightness[3][0]),
                              Math.abs(vertexBrightness[3][0]-vertexBrightness[0][0])};
      if(transparency[0] > 0.0001 || transparency[1] > 0.0001 || transparency[2] > 0.0001 || transparency[3] > 0.0001)
        flags&=-17;
    }
    vertexBrightness[0][1] = brightnessLevels[0][start];
    vertexBrightness[0][2] = brightnessLevels[0][start+1];
    vertexBrightness[0][3] = brightnessLevels[0][start+2];
    vertexBrightness[1][1] = brightnessLevels[1][start];
    vertexBrightness[1][2] = brightnessLevels[1][start+1];
    vertexBrightness[1][3] = brightnessLevels[1][start+2];
    vertexBrightness[2][1] = brightnessLevels[2][start];
    vertexBrightness[2][2] = brightnessLevels[2][start+1];
    vertexBrightness[2][3] = brightnessLevels[2][start+2];
    vertexBrightness[3][1] = brightnessLevels[3][start];
    vertexBrightness[3][2] = brightnessLevels[3][start+1];
    vertexBrightness[3][3] = brightnessLevels[3][start+2];
  }
  
  
  public float[] returnVertexBrightness(byte index){
    return vertexBrightness[index];
  }
  public float[][] returnVertexBrightness(){
    return vertexBrightness;
  }

  //Returns a string detail the state of the current instance of Rect
  public String toString(){
     String fillString = "FILL: "+fill+"\n";
     String strokeString = "STROKE: "+stroke+"\n";
     String hasFillString = "HAS FILL: "+((flags & 2) == 2)+"\n";
     String hasStrokeString = "HAS STROKE: "+((flags & 1) == 1)+"\n";
     String vertexString = "";
     for(byte i = 0; i < 4; i++)
       vertexString+="VERTEX "+(i+1)+": ("+vertices[i][0]+", "+vertices[i][1]+", "+vertices[i][2]+")\n";
     return fillString+strokeString+hasFillString+hasStrokeString+vertexString;
  }
  
  //Copies an object's state to the current instance of Rect
  public void copy(Object o){
    if(o instanceof Quad){
      Quad r = (Quad)o;
      image = r.image;
      mode = r.mode;
      fill = r.fill;
      stroke = r.stroke;
      flags = r.flags;
      stencil = r.stencil;
      for(byte i = 0; i < 4; i++){
        for(byte j = 0; j < 3; j++){
          vertices[i][j] = r.vertices[i][j];   
          vertexBrightness[i][j] = r.vertexBrightness[i][j];
        }
        vertexBrightness[i][3] = r.vertexBrightness[i][3];
      }
    }
    else
       System.out.println("ERROR: OBJECT IS NOT OF TYPE RECT"); 
  }
  
  public void copy(Quad r){
    image = r.image;
    mode = r.mode;
    fill = r.fill;
    stroke = r.stroke;
    stencil = r.stencil;
    flags = r.flags;
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = r.vertices[i][j];   
        vertexBrightness[i][j] = r.vertexBrightness[i][j];
       }
       vertexBrightness[i][3] = r.vertexBrightness[i][3];
     }
  }
  
  //Tests if an object is equal to the current instance of Rect
  public boolean equals(Object o){
    if(o instanceof Quad){
      Quad r = (Quad)o;
      boolean isEqual = true;
      isEqual&=(image.equals(r.image));
      isEqual&=(mode == r.mode);
      isEqual&=(fill == r.fill);
      isEqual&=(stroke == r.stroke);
      isEqual&=(flags == r.flags);
      isEqual&=(stencil == r.stencil);
      for(byte i = 0; i < 4; i++){
        for(byte j = 0; j < 3; j++){
          isEqual&=(Math.abs(vertices[i][j] - r.vertices[i][j]) <= 0.0001);  
          isEqual&=(Math.abs(vertexBrightness[i][j] - r.vertexBrightness[i][j]) <= 0.0001);  
        }
        isEqual&=(Math.abs(vertexBrightness[i][3] - r.vertexBrightness[i][3]) <= 0.0001);
      }
      return isEqual;
    }
    else
      return false;
  }
  public boolean equals(Quad r){
    boolean isEqual = true;
    isEqual&=(image.equals(r.image));
    isEqual&=(mode == r.mode);
    isEqual&=(fill == r.fill);
    isEqual&=(stroke == r.stroke);
    isEqual&=(flags == r.flags);
    isEqual&=(stencil == r.stencil);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        isEqual&=(Math.abs(vertices[i][j] - r.vertices[i][j]) <= 0.0001);  
        isEqual&=(Math.abs(vertexBrightness[i][j] - r.vertexBrightness[i][j]) <= 0.0001);  
      }
      isEqual&=(Math.abs(vertexBrightness[i][3] - r.vertexBrightness[i][3]) <= 0.0001);
    }
    return isEqual;
  }
}
