public class Quad extends FilledParent{
  public static final int[][] TRI_INDICES = {{0, 2, 3}, {0, 1, 2}};
  private Graphic image;
  //r = replace pixels and do not draw areas where specified by graphic
  //m = multiply pixels and do not draw areas where specified by graphic
  //k = replace pixels; areas that are not to be replaced will be fill colour
  //u = multiply pixels; areas that are not to be replaced will be fill colour
  private char mode = 'r';
  private float[][] vertices = new float[4][3];
  private float[][] vertexBrightness = new float[4][4];
  //Flag bits
  //3 = Equal Transparencies
  //4 = Equal Colour
  //5 = Removal enable, 
  //6 = parallel sides equal length
  
  public Quad(){
    super((byte)92);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = 0;
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
  }
  
  public Quad(float[][] newVerts, int newFill, int newStroke, boolean hasFill, boolean hasStroke){
    super(newStroke, newFill, hasStroke, hasFill);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    float[] diffX = {Math.abs(vertices[1][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[3][0]),
                     Math.abs(vertices[3][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[1][0])};
    float[] diffY = {Math.abs(vertices[1][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[3][1]),
                     Math.abs(vertices[3][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[1][1])};
    if(diffX[0] <= 0.0001 && diffY[0] <= 0.0001 && diffX[1] <= 0.0001 && diffY[1] <= 0.0001)
      flags|=64;
    flags|=24;
  }
  public Quad(float[][] newVerts, Graphic img){
    super((byte)28);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    float[] diffX = {Math.abs(vertices[1][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[3][0]),
                     Math.abs(vertices[3][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[1][0])};
    float[] diffY = {Math.abs(vertices[1][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[3][1]),
                     Math.abs(vertices[3][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[1][1])};
    if(diffX[0] <= 0.0001 && diffY[0] <= 0.0001 && diffX[1] <= 0.0001 && diffY[1] <= 0.0001)
      flags|=64;
    image = img;
  }
  public Quad(float[][] newVerts, Graphic img, int newFill, int newStroke, boolean hasFill, boolean hasStroke){
    super(newStroke, newFill, hasStroke, hasFill);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVerts[i][j];
        vertexBrightness[i][j] = 1;
      }
      vertexBrightness[i][3] = 1;
    }
    float[] diffX = {Math.abs(vertices[1][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[3][0]),
                     Math.abs(vertices[3][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[1][0])};
    float[] diffY = {Math.abs(vertices[1][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[3][1]),
                     Math.abs(vertices[3][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[1][1])};
    if(diffX[0] <= 0.0001 && diffY[0] <= 0.0001 && diffX[1] <= 0.0001 && diffY[1] <= 0.0001)
      flags|=64;
    flags|=24;
    image = img;
  }

  public void setImage(Graphic img){
    image = img;
  }
  
  //Setting the rect's vertices
  public void setVertices(float[][] newVertices){
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        vertices[i][j] = newVertices[i][j];
      }
    }
    float[] diffX = {Math.abs(vertices[1][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[3][0]),
                     Math.abs(vertices[3][0] - vertices[0][0])-Math.abs(vertices[2][0]-vertices[1][0])};
    float[] diffY = {Math.abs(vertices[1][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[3][1]),
                     Math.abs(vertices[3][1] - vertices[0][1])-Math.abs(vertices[2][1]-vertices[1][1])};

    if(diffX[0] <= 0.0001 && diffY[0] <= 0.0001 && diffX[1] <= 0.0001 && diffY[1] <= 0.0001)
      flags|=64;
    else
      flags&=-65;
  }
  
  public void setMode(char newMode){
    switch(newMode){
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
      flags|=32;
    else
      flags&=-33;
  }

  public boolean equalTransparencies(){
    return (flags & 8) == 8;
  }

  public boolean equalColour(){
    return (flags & 16) == 16;
  } 


  public boolean hasRemoval(){
    return (flags & 32) == 32;
  }
  
  public boolean isRectangle(){
    return (flags & 64) == 64;
  }

  public char returnMode(){
    return mode;
  }
  public int[] returnPixels(){
    if(image != null)
      return image.returnPixels();
    return null;
  }
  public boolean hasImage(){
    return image != null;
  }
  public int returnInvisColour(){
    return image.returnInvisColour((byte)0);
  }
  public boolean shouldDrawPixel(int x, int y){
    return image.shouldDrawPixel(x, y) || (flags & 32) == 32;
  }
  public int returnImageWidth(){
    if(image != null)
      return image.returnWidth();
    else
      return 0;
  }
  public int returnImageHeight(){
    if(image != null)
      return image.returnHeight();
    else
      return 0;
  }
  //Returns a specific vertex position
  public float getVertexPosition(byte vertex, byte axis){
     if(vertex >= 0 && vertex <= 3 && axis >= 0 && axis < 3)
       return vertices[vertex][axis];
     else{
       System.out.println("ERROR: VERTEX DOES NOT EXIST");
       System.exit(-1);
       return Float.intBitsToFloat(-1);
     }
  }
  //Returns the reference to all vertices
  public float[][] getVertices(){
    float[][] returnVerts = {{vertices[0][0], vertices[0][1], vertices[0][2]},
                             {vertices[1][0], vertices[1][1], vertices[1][2]},
                             {vertices[2][0], vertices[2][1], vertices[2][2]},
                             {vertices[3][0], vertices[3][1], vertices[3][2]}};
    return returnVerts; 
  }
  
  //Returns centre position
  public float returnX(){
    return (vertices[0][0]+vertices[1][0]+vertices[2][0]+vertices[3][0])*0.25f; 
  }
  
  public float returnY(){
    return (vertices[0][1]+vertices[1][1]+vertices[2][1]+vertices[3][1])*0.25f;
  }
  
  public float returnZ(){
    return (vertices[0][2]+vertices[1][2]+vertices[2][2]+vertices[3][2])*0.25f;
  }

  public float[] returnPosition(){
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
    setEqualityFlags();
  }
  
  public void setVertexBrightness(float a, float r, float g, float b, byte index){
    vertexBrightness[index][0] = a;
    vertexBrightness[index][1] = r;
    vertexBrightness[index][2] = g;
    vertexBrightness[index][3] = b;
    setEqualityFlags();
    
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
    setEqualityFlags();
  }

  public void setVertexBrightness(float[][] brightnessLevels){
    byte start = 0;
    if(brightnessLevels[0].length >= 4){
      start = 1;
      vertexBrightness[0][0] = brightnessLevels[0][0];
      vertexBrightness[1][0] = brightnessLevels[1][0];
      vertexBrightness[2][0] = brightnessLevels[2][0];
      vertexBrightness[3][0] = brightnessLevels[3][0];
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
    setEqualityFlags();
  }

  private void setEqualityFlags(){
    float[] transparency = {Math.abs(vertexBrightness[0][0]-vertexBrightness[1][0])+Math.abs(vertexBrightness[1][0]-vertexBrightness[2][0])+Math.abs(vertexBrightness[2][0]-vertexBrightness[3][0])+Math.abs(vertexBrightness[3][0]-vertexBrightness[0][0]),
                            Math.abs(vertexBrightness[0][1]-vertexBrightness[1][1])+Math.abs(vertexBrightness[1][1]-vertexBrightness[2][1])+Math.abs(vertexBrightness[2][1]-vertexBrightness[3][1])+Math.abs(vertexBrightness[3][1]-vertexBrightness[0][1]),
                            Math.abs(vertexBrightness[0][2]-vertexBrightness[1][2])+Math.abs(vertexBrightness[1][2]-vertexBrightness[2][2])+Math.abs(vertexBrightness[2][2]-vertexBrightness[3][2])+Math.abs(vertexBrightness[3][2]-vertexBrightness[0][2]),
                            Math.abs(vertexBrightness[0][3]-vertexBrightness[1][3])+Math.abs(vertexBrightness[1][3]-vertexBrightness[2][3])+Math.abs(vertexBrightness[2][3]-vertexBrightness[3][3])+Math.abs(vertexBrightness[3][3]-vertexBrightness[0][3])};
    if(transparency[0] <= 0.0001)
      flags|=8;
    else
      flags&=-9;
      
    if(transparency[1] <= 0.0001 && transparency[2] <= 0.0001 && transparency[3] <= 0.0001)
      flags|=16;
    else
      flags&=-17;
  }
  
  
  public float[] returnVertexBrightness(byte index){
    float[] copy = {vertexBrightness[index][0], vertexBrightness[index][1], vertexBrightness[index][2], vertexBrightness[index][3]};
    return copy;
  }
  public float[][] returnVertexBrightness(){
    float[][] copy = {{vertexBrightness[0][0], vertexBrightness[0][1], vertexBrightness[0][2], vertexBrightness[0][3]},
                      {vertexBrightness[1][0], vertexBrightness[1][1], vertexBrightness[1][2], vertexBrightness[1][3]},
                      {vertexBrightness[2][0], vertexBrightness[2][1], vertexBrightness[2][2], vertexBrightness[2][3]},
                      {vertexBrightness[3][0], vertexBrightness[3][1], vertexBrightness[3][2], vertexBrightness[3][3]}};

    return copy;
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
      super.copy(r);
      image = r.image;
      mode = r.mode;
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
    super.copy(r);
    image = r.image;
    mode = r.mode;
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
      boolean isEqual = super.equals(r);
      isEqual&=(image.equals(r.image));
      isEqual&=(mode == r.mode);
      for(byte i = 0; i < 4; i++){
        for(byte j = 0; j < 3; j++){
          isEqual&=(Math.abs(vertices[i][j] - r.vertices[i][j]) <= EPSILON);  
          isEqual&=(Math.abs(vertexBrightness[i][j] - r.vertexBrightness[i][j]) <= EPSILON);  
        }
        isEqual&=(Math.abs(vertexBrightness[i][3] - r.vertexBrightness[i][3]) <= EPSILON);
      }
      return isEqual;
    }
    else
      return false;
  }
  public boolean equals(Quad r){
    boolean isEqual = super.equals(r);
    isEqual&=(image.equals(r.image));
    isEqual&=(mode == r.mode);
    for(byte i = 0; i < 4; i++){
      for(byte j = 0; j < 3; j++){
        isEqual&=(Math.abs(vertices[i][j] - r.vertices[i][j]) <= EPSILON);  
        isEqual&=(Math.abs(vertexBrightness[i][j] - r.vertexBrightness[i][j]) <= EPSILON);  
      }
      isEqual&=(Math.abs(vertexBrightness[i][3] - r.vertexBrightness[i][3]) <= EPSILON);
    }
    return isEqual;
  }
}