import java.util.*;
import java.io.*;
public class QuadDraw{
  private static final float EPSILON = 0.0000001f;
  private static int minTransparency = 0;
  private static byte flags = 4; //0 = stencilTestResults, 4 = depthWrite, 8 = hasStroke, 16 = hasFill
  private static int[] frame = new int[100000];
  private static float[] zBuff = new float[100000];
  private static byte[] stencil = new byte[100000];
  private static float alpha = 0;
  private static float beta = 0;
  private static float gamma = 1;
  private static int fill = 0;
  private static int stroke = 0;
  private static float alphaNorm = 1;
  private static int[] brokenUpColour = {0, 0, 0, 0}; //Temporary storage for the final pixel colour
  private static int[] brokenUpFill = {0, 0, 0, 0}; //Holds the component RGB channels of the fill
  private static int[] brokenUpSprite = {0, 0, 0};
  private static float threshold = 1.1f;
  private static StencilAction tempAction = new StencilAction();
  private static int wid = 100;
  private static int heig = 100;
  private static int meshSize = 2;
  private static boolean[] mesh = {true, false, false, true};

  public static void setFrame(int[] newFrame, int width, int height){
    frame = newFrame;
    wid = width;
    heig = height;
    zBuff = new float[width*height];
    stencil = new byte[width*height];
    for(int i = 0; i < zBuff.length; i++){
      zBuff[i] = Float.NaN;
      stencil[i] = (byte)0;
    }
  }
  
  public static void useMeshTransparency(){
    flags|=-128;
  }

  public static boolean loadTransparencyMesh(String file){
    boolean failed = false;
    File newMesh = new File(file);
    try{
      Scanner fileReader = new Scanner(newMesh);
      while(!failed){
        if(fileReader.hasNextLine()){
          Scanner stringReader = new Scanner(fileReader.nextLine());
          if(stringReader.hasNextInt()){
            meshSize = stringReader.nextInt();
            stringReader.close();
            break;
          }
          else if(stringReader.hasNext() && stringReader.next().charAt(0) != '#')
            failed = true;
          stringReader.close();
        }
        else
          failed = true;
      }
      if(meshSize < 2)
        failed = true;
      if(!failed){
        int meshIndex = 0;
        mesh = new boolean[meshSize*meshSize];
        while(!failed && fileReader.hasNext() && meshIndex < mesh.length){
          if(fileReader.hasNextInt()){
            switch(fileReader.nextInt()){
              case 0:
                mesh[meshIndex] = false;
                break;
              case 1:
                mesh[meshIndex] = true;
                break;
              default:
                failed = true;
            }
            meshIndex++;
          }
          else{
            if(fileReader.next().contains("#"))
              fileReader.nextLine();
            else
              failed = true;
          }
        }
        for(; meshIndex < mesh.length; meshIndex++)
          mesh[meshIndex] = true;
      }
      fileReader.close();
    }
    catch(Exception e){
      System.out.println("ERROR: FILE "+file+" NOT FOUND OR IS INVALID");
      e.printStackTrace();
      failed = true;
    }
    if(failed){
      meshSize = 2;
      mesh = new boolean[4];
      mesh[0] = true;
      mesh[1] = false;
      mesh[2] = false;
      mesh[3] = true;
    }
    return !failed;
  }
  public static void useAlphaTransparency(){
    flags&=127;
  }

  //Setting the fill of the current rect
  public static void fill(short r, short g, short b){
    flags|=16;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    fill = 0xFF000000|(r << 16)|(g << 8)|b;
    brokenUpFill[0] = 0xFF;
    alphaNorm = 1;
    brokenUpFill[0] = r;
    brokenUpFill[1] = g;
    brokenUpFill[2] = b;
  }
  public static void fill(short r, short g, short b, short a){
    flags|=16;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    a&=0xFF;
    fill = (a << 24)|(r << 16)|(g << 8)|b;
    brokenUpFill[0] = a;
    alphaNorm = a*Colour.INV_255;
    brokenUpFill[1] = r;
    brokenUpFill[2] = g;
    brokenUpFill[3] = b;
  }
  public static void fill(int colour){
    flags|=16;
    if((colour >>> 24) == 0){
      if(colour <= 0xFF)
          colour = 0xFF000000 | (colour << 16) | (colour << 8) | colour;
      else if(colour <= 0xFFFF)
          colour = ((colour & 0xFF00) << 16) | ((colour & 0xFF) << 16) | ((colour & 0xFF) << 8) | (colour & 0xFF);
      else
          colour = 0xFF000000 | colour;
    }
    fill = colour;
    brokenUpFill[0] = fill >>> 24;
    alphaNorm = brokenUpFill[0]*Colour.INV_255;
    brokenUpFill[1] = (fill >>> 16) & 0xFF;
    brokenUpFill[2] = (fill >>> 8) & 0xFF;
    brokenUpFill[3] = fill & 0xFF;
  }
  public static void fill(int colour, short alpha){
    flags|=16;
    alpha&=0xFF;
    colour = (colour & 0xFFFFFF);
    if(colour > 0xFF)
      fill = (alpha << 24)|colour;
    else
      fill = (alpha << 24)|(colour << 16)|(colour << 8)|colour;
    brokenUpFill[0] = alpha;
    alphaNorm = alpha*Colour.INV_255;
    brokenUpFill[1] = (fill >>> 16) & 0xFF;
    brokenUpFill[2] = (fill >>> 8) & 0xFF;
    brokenUpFill[3] = fill & 0xFF;
  }
  
  //Setting the stroke colour of the rect
  public static void stroke(short r, short g, short b){
    flags|=8;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    stroke = 0xFF000000|(r << 16)|(g << 8)|b;
  }
  public static void stroke(short r, short g, short b, short a){
    flags|=8;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    a&=0xFF;
    stroke = (a << 24)|(r << 16)|(g << 8)|b;
  }
  public static void stroke(int colour){
    flags|=8;
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
  public static void stroke(int colour, short alpha){
    flags|=8;
    alpha&=0xFF;
    colour = (colour & 0xFFFFFF);
   if(colour > 0xFF)
      stroke = (alpha << 24)|colour;
    else
      stroke = (alpha << 24)|(colour << 16)|(colour << 8)|colour;
  }
  
  //Selecting having no fill
  public static void noFill(){
    flags&=-17;
  }
  //Selecting having no stroke
  public static void noStroke(){
    flags&=-9;
  }
  
  public static void setDepthWrite(boolean depthEnable){
     if(depthEnable)
       flags|=4;
     else
       flags&=-5;
  }
  
  public static void setProbabilities(float newMax, float newThreshold){
    if(newMax < -EPSILON || newMax > EPSILON)
      threshold = Math.abs(newThreshold/newMax);
    else
      threshold = 1;
  }
  
  public static void drawQuad(Quad sprite){

    if(sprite.getHasStroke()){
      stroke = sprite.returnStroke();
      flags|=8;
    }
    else
      flags&=-9;
    
    if(sprite.getHasFill()){
      fill = sprite.returnFill();
      flags|=16;
    }
    else
      flags&=-17;

    brokenUpFill[0] = fill >>> 24;
    boolean alwaysDraw = (flags & -128) == 0 || brokenUpFill[0] >= 0;
    int[] texels = sprite.returnPixels();
    int spriteWidth = sprite.returnImageWidth();
    int spriteHeight = sprite.returnImageHeight();
    char spriteMode = sprite.returnMode();
    float[][] vertexColours = sprite.returnVertexBrightness();
    float[][] vertices = sprite.getVertices();
    if(sprite.equalTransparencies()){
        brokenUpFill[0] = (int)(brokenUpFill[0]*vertexColours[0][0]);
    }
    if((flags & -128) == -128){
      brokenUpFill[0] = 255;
      stroke|=0xFF000000;
      vertexColours[0][0] = 1;
    }

    alphaNorm = brokenUpFill[0]*Colour.INV_255;
    brokenUpFill[1] = (fill >>> 16) & 0xFF;
    brokenUpFill[2] = (fill >>> 8) & 0xFF;
    brokenUpFill[3] = fill & 0xFF;

   
    
    vertices[0][2]*=(((flags & 4) >>> 1)-1);
    vertices[1][2]*=(((flags & 4) >>> 1)-1);
    vertices[2][2]*=(((flags & 4) >>> 1)-1);
    vertices[3][2]*=(((flags & 4) >>> 1)-1);

    //Determines if there is a self intersection between the sides
    boolean hasIntersection = hasIntersection(vertices[0][0], vertices[0][1], vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], false);
    hasIntersection|=hasIntersection(vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], vertices[0][0], vertices[0][1], false);
    if(hasIntersection){
      System.out.println("INTERSECTION");
      return;
    }
    float[] denominators = {(vertices[2][1] - vertices[3][1])*(vertices[0][0] - vertices[3][0]) + (vertices[3][0] - vertices[2][0])*(vertices[0][1] - vertices[3][1]),
                            (vertices[1][1] - vertices[2][1])*(vertices[0][0] - vertices[2][0]) + (vertices[2][0] - vertices[1][0])*(vertices[0][1] - vertices[2][1])};
      
    if(denominators[0] >= -EPSILON && denominators[0] <= EPSILON || denominators[1] >= -EPSILON && denominators[1] <= EPSILON){
      return;
    }
    //Quad fill
    if((flags & 16) == 16){

      float[] adjWeights = {1, 1, 1, 1};
      boolean useImage = sprite.hasImage();
      float[] intersect = {Float.NaN, Float.NaN};
      if(!sprite.isRectangle()){
        intersect = getIntersection(vertices[0][0], vertices[0][1], vertices[2][0], vertices[2][1], vertices[1][0], vertices[1][1], vertices[3][0], vertices[3][1]);
        if(!Float.isNaN(intersect[0]) && !Float.isNaN(intersect[1])){
          float[] dists = {(float)Math.sqrt((vertices[0][0]-intersect[0])*(vertices[0][0]-intersect[0]) + (vertices[0][1]-intersect[1])*(vertices[0][1]-intersect[1])),
                           (float)Math.sqrt((vertices[1][0]-intersect[0])*(vertices[1][0]-intersect[0]) + (vertices[1][1]-intersect[1])*(vertices[1][1]-intersect[1])),
                           (float)Math.sqrt((vertices[2][0]-intersect[0])*(vertices[2][0]-intersect[0]) + (vertices[2][1]-intersect[1])*(vertices[2][1]-intersect[1])),
                           (float)Math.sqrt((vertices[3][0]-intersect[0])*(vertices[3][0]-intersect[0]) + (vertices[3][1]-intersect[1])*(vertices[3][1]-intersect[1]))};
                            
          if(dists[2] > EPSILON)
            adjWeights[0] = (dists[0]/dists[2] + 1);
          if(dists[3] > EPSILON)
            adjWeights[1] = (dists[1]/dists[3] + 1);
          if(dists[0] > EPSILON)
            adjWeights[2] = (dists[2]/dists[0] + 1);
          if(dists[1] > EPSILON)
            adjWeights[3] = (dists[3]/dists[1] + 1);
        }
        else{
          return;
        }
      }

      //Grabbing the x-boundries and y-boundries of the quadrilateral
      int[] xBounds = {Math.round(Math.max(0, Math.min(vertices[0][0], Math.min(vertices[1][0], Math.min(vertices[2][0], vertices[3][0]))))),
                       Math.round(Math.min(wid, Math.max(vertices[0][0], Math.max(vertices[1][0], Math.max(vertices[2][0], vertices[3][0])))))};
      int[] yBounds = {Math.round(Math.max(0, Math.min(vertices[0][1], Math.min(vertices[1][1], Math.min(vertices[2][1], vertices[3][1]))))),
                       Math.round(Math.min(heig, Math.max(vertices[0][1], Math.max(vertices[1][1], Math.max(vertices[2][1], vertices[3][1])))))};
      int minX = xBounds[0];
      int maxX = xBounds[1];
      denominators[0] = 1/denominators[0];
      denominators[1] = 1/denominators[1];
      float[] denominator = {vertices[1][1]-vertices[0][1]-EPSILON, 
                             vertices[2][1]-vertices[1][1]-EPSILON,
                             vertices[3][1]-vertices[2][1]-EPSILON,
                             vertices[0][1]-vertices[3][1]-EPSILON};

      if(denominator[0] < -EPSILON || denominator[0] > EPSILON)
        denominator[0] = 1/denominator[0];
      else
        denominator[0] = Float.NaN;
      if(denominator[1] < -EPSILON || denominator[1] > EPSILON)
        denominator[1] = 1/denominator[1];
      else
        denominator[1] = Float.NaN;
      if(denominator[2] < -EPSILON || denominator[2] > EPSILON)
        denominator[2] = 1/denominator[2];
      else
        denominator[2] = Float.NaN;
      if(denominator[3] < -EPSILON || denominator[3] > EPSILON)
        denominator[3] = 1/denominator[3];
      else
        denominator[3] = Float.NaN;
      //Stores the x-location of the interpolated edges
      float[] interpolatedEdges = {0, 0, 0, 0};

      float xPos = 0;
      float yPos = 0;
      int imgX = 0;
      int imgY = 0;
      int pixelPos = 0;
      int uvIndex = 0;
      float z = 0;
      float tempZ = 0;
      float u = 0;
      float v = 0;
      float adjustedAlpha = 0;
      float adjustedBeta = 0;
      float adjustedGamma = 0;
      float overallWeight = 0;

      int[] indices;
      float[] t = {-1, -1, -1, -1};

      for(int i = yBounds[0]; i < yBounds[1]; i++){
        //The current valid edge
        byte currentEdge = 1;
        //The centre of the pixel on the y-axis
        yPos = i+0.5f;
        //How far along the edge the scanline is
        
        //Computing all four t values
        t[0] = (vertices[1][1]-yPos)*denominator[0]-EPSILON;
        t[1] = (vertices[2][1]-yPos)*denominator[1]-EPSILON;
        t[2] = (vertices[3][1]-yPos)*denominator[2]-EPSILON;
        t[3] = (vertices[0][1]-yPos)*denominator[3]-EPSILON;
        
        //For when there are three or fewer valid edges (t is outside of the range of 0 and 1)
        //Computing the x-position of each edge if it is valid
        if(t[0] >= 0 && t[0] <= 1){
          interpolatedEdges[currentEdge] = (vertices[0][0]-vertices[1][0])*t[0]+vertices[1][0]-EPSILON;
          currentEdge--;
        }
        if(t[1] >= 0 && t[1] <= 1){
          interpolatedEdges[currentEdge] = (vertices[1][0]-vertices[2][0])*t[1]+vertices[2][0]-EPSILON;
          if(currentEdge > 0)
            currentEdge--;
          else
            currentEdge = 0;
        }
        if(t[2] >= 0 && t[2] <= 1){
          interpolatedEdges[currentEdge] = (vertices[2][0]-vertices[3][0])*t[2]+vertices[3][0]-EPSILON;
          if(currentEdge > 0)
            currentEdge--;
          else
            currentEdge = 0;
        }
        if(t[3] >= 0 && t[3] <= 1){
          interpolatedEdges[0] = (vertices[3][0]-vertices[0][0])*t[3]+vertices[0][0]-EPSILON;
        }
        //Finding the left-most edge and right most edge and locking them to be in between the left and right of the screen
        if(interpolatedEdges[0] <= interpolatedEdges[1]){
          xBounds[0] = Math.round(interpolatedEdges[0]);
          xBounds[1] = Math.round(interpolatedEdges[1]);
        }
        else{
          xBounds[0] = Math.round(interpolatedEdges[1]);
          xBounds[1] = Math.round(interpolatedEdges[0]);
        }
        if(xBounds[0] <= minX)
          xBounds[0] = minX;
        if(xBounds[1] >= maxX)
          xBounds[1] = maxX;

        //Drawing between the edges
        for(int j = xBounds[0]; j < xBounds[1]; j++){
          pixelPos = i*wid+j;
          if((alwaysDraw || mesh[(i%meshSize)*meshSize+(j%meshSize)]) && stencil[pixelPos] == 0 && (1 <= threshold || Math.random() < threshold)){ 
            xPos = j+0.5f; //The centre-x of the pixel
            boolean draw = true;//Determines if the current pixel should be updated
            indices = Quad.TRI_INDICES[0];
            uvIndex = 0;
              
            //Computing the weights of the current triangle
            alpha = ((vertices[indices[1]][1] - vertices[indices[2]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[2]][0] - vertices[indices[1]][0])*(yPos - vertices[indices[2]][1]))*denominators[0];
            beta = ((vertices[indices[2]][1] - vertices[indices[0]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[0]][0] - vertices[indices[2]][0])*(yPos - vertices[indices[2]][1]))*denominators[0];
            gamma = 1-alpha-beta;
            if(alpha < 0 || beta < 0 || gamma < 0){
              indices = Quad.TRI_INDICES[1];
              uvIndex = 1;
              alpha = ((vertices[indices[1]][1] - vertices[indices[2]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[2]][0] - vertices[indices[1]][0])*(yPos - vertices[indices[2]][1]))*denominators[1];
              beta = ((vertices[indices[2]][1] - vertices[indices[0]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[0]][0] - vertices[indices[2]][0])*(yPos - vertices[indices[2]][1]))*denominators[1];
              gamma = 1-alpha-beta;
            }
  
            adjustedAlpha = adjWeights[indices[0]]*alpha;
            adjustedBeta = adjWeights[indices[1]]*beta;
            adjustedGamma = adjWeights[indices[2]]*gamma;
            if(useImage){
              overallWeight = alpha*adjWeights[indices[0]]+beta*adjWeights[indices[1]]+gamma*adjWeights[indices[2]];
              if(overallWeight > EPSILON)
                overallWeight = 1/overallWeight;
              else
                overallWeight = EPSILON;
              //Finding the UV coordinates of the texture in the current triangle
              //(Vertex A is guarenteed to be constant)
              //(For U, vertices B and C are both 1, though D can be 0)
              //(For V, vertices D and C are both 0, but B can be 1)
              u = (adjustedBeta+(uvIndex*adjustedGamma))*overallWeight;
              v = (adjustedAlpha+(uvIndex*adjustedBeta))*overallWeight;
              if(u >= 0 && u <= 1 && v >= 0 && v <= 1){
            
                //Converting from UV coordinates to the real coordinates in the image
                imgX = (int)(u*spriteWidth);
                imgY = (int)(v*spriteHeight);
                if(imgX >= spriteWidth)
                  imgX = spriteWidth-1;
                else if(imgX < 0)
                  imgX = 0;
                if(imgY >= spriteHeight)
                  imgY = spriteHeight-1;
                else if(imgY < 0)
                  imgY = 0;
                //Grabbing the colour of the current pixel in the image
                draw = sprite.shouldDrawPixel(imgX, imgY);
                int spritePixel = texels[imgX+spriteWidth*imgY];
                //Breaking up the colours the image into their component RGB channels
          
                brokenUpSprite[0] = (spritePixel >>> 16) & 0xFF;
                brokenUpSprite[1] = (spritePixel >>> 8) & 0xFF;
                brokenUpSprite[2] = spritePixel & 0xFF;
                if(draw){
                    if(spriteMode == 'm' || spriteMode == 'u'){
                      //Multiplying the fill's channels by the image's channels
                      brokenUpColour[0] = (int)(brokenUpFill[0]);
                      brokenUpColour[1] = (int)(brokenUpSprite[0]*brokenUpFill[1]*Colour.INV_255); 
                      brokenUpColour[2] = (int)(brokenUpSprite[1]*brokenUpFill[2]*Colour.INV_255); 
                      brokenUpColour[3] = (int)(brokenUpSprite[2]*brokenUpFill[3]*Colour.INV_255);
                    }
                    else{
                      brokenUpColour[0] = 0xFF;
                      brokenUpColour[1] = brokenUpSprite[0];
                      brokenUpColour[2] = brokenUpSprite[1];
                      brokenUpColour[3] = brokenUpSprite[2];
                    }
                  }
                }
              }

              if(!useImage || spriteMode == 'u' || spriteMode == 'k'){
                draw = true;
                brokenUpColour[0] = brokenUpFill[0];
                brokenUpColour[1] = brokenUpFill[1];
                brokenUpColour[2] = brokenUpFill[2];
                brokenUpColour[3] = brokenUpFill[3];
              }
              //Updating the pixel
              if(draw){
                int brokenUpFrame[] = {frame[pixelPos] >>> 24, (frame[pixelPos] >>> 16) & 0xFF, (frame[pixelPos] >>> 8) & 0xFF, frame[pixelPos] & 0xFF};
                z = vertices[indices[0]][2]*alpha+vertices[indices[1]][2]*beta+vertices[indices[2]][2]*gamma;
                adjustedAlpha*=vertices[indices[0]][2];
                adjustedBeta*=vertices[indices[1]][2];
                adjustedGamma*=vertices[indices[2]][2];
                tempZ = adjustedAlpha+adjustedBeta+adjustedGamma;
                if(tempZ < -EPSILON || tempZ > EPSILON)
                  tempZ = 1/tempZ;
                else
                  tempZ = EPSILON*(((flags & 4) >>> 1)-1);
               
                if(Float.isNaN(zBuff[pixelPos]) || ((flags & 4) == 0 && z <= zBuff[pixelPos] || zBuff[pixelPos] >= 0 && z > zBuff[pixelPos])){
                  if(!sprite.equalTransparencies() && (flags & -128) == 0)
                    brokenUpColour[0] = Math.round(brokenUpColour[0]*tempZ*(vertexColours[indices[0]][0]*adjustedAlpha+vertexColours[indices[1]][0]*adjustedBeta+vertexColours[indices[2]][0]*adjustedGamma));
                  else
                    brokenUpColour[0] = Math.round(brokenUpColour[0]*vertexColours[0][0]);
                  if(brokenUpColour[0] >= 255)
                    brokenUpColour[0] = 255;
                  else if(brokenUpColour[0] <= 0)
                    brokenUpColour[0] = 0;

                if(brokenUpColour[1] != 0 && brokenUpColour[2] != 0 && brokenUpColour[3] != 0){
                  if(!sprite.equalColour()){
                    float[] overallBrightness = {tempZ*(vertexColours[indices[0]][1]*adjustedAlpha+vertexColours[indices[1]][1]*adjustedBeta+vertexColours[indices[2]][1]*adjustedGamma),
                                                 tempZ*(vertexColours[indices[0]][2]*adjustedAlpha+vertexColours[indices[1]][2]*adjustedBeta+vertexColours[indices[2]][2]*adjustedGamma),
                                                 tempZ*(vertexColours[indices[0]][3]*adjustedAlpha+vertexColours[indices[1]][3]*adjustedBeta+vertexColours[indices[2]][3]*adjustedGamma)};
                    if(overallBrightness[0] <= 0)
                      overallBrightness[0] = 0;
                    if(overallBrightness[1] <= 0)
                      overallBrightness[1] = 0;
                    if(overallBrightness[2] <= 0)
                      overallBrightness[2] = 0;

                    brokenUpColour[1] = Math.round(brokenUpColour[1]*overallBrightness[0]);
                    brokenUpColour[2] = Math.round(brokenUpColour[2]*overallBrightness[1]);
                    brokenUpColour[3] = Math.round(brokenUpColour[3]*overallBrightness[2]);
                  }
                  else{
                    brokenUpColour[1] = Math.round(brokenUpColour[1]*vertexColours[0][1]);
                    brokenUpColour[2] = Math.round(brokenUpColour[2]*vertexColours[0][2]);
                    brokenUpColour[3] = Math.round(brokenUpColour[3]*vertexColours[0][3]);
                    if(brokenUpColour[1] <= 0)
                      brokenUpColour[1] = 0;
                    if(brokenUpColour[2] <= 0)
                      brokenUpColour[2] = 0;
                    if(brokenUpColour[3] <= 0)
                      brokenUpColour[3] = 0;
                  }
                  if(brokenUpColour[1] >= 255)
                    brokenUpColour[1] = 255;
                  if(brokenUpColour[2] >= 255)
                    brokenUpColour[2] = 255;
                  if(brokenUpColour[3] >= 255)
                    brokenUpColour[3] = 255;
                }

                    if(brokenUpColour[0] < 0xFF)
                      Colour.interpolateColours(brokenUpColour, brokenUpFrame);
                    if(brokenUpColour[0] > minTransparency){
                      frame[pixelPos] = (brokenUpColour[0] << 24)|(brokenUpColour[1] << 16)|(brokenUpColour[2] << 8)|brokenUpColour[3];
                           
                    zBuff[pixelPos] = z;
                  }
                }
                else if(brokenUpFrame[0] < 0xFF){
                  Colour.interpolateColours(brokenUpFrame, brokenUpColour);
                  if(brokenUpFrame[0] > minTransparency)
                    frame[pixelPos] = (brokenUpFrame[0] << 24)|(brokenUpFrame[1] << 16)|(brokenUpFrame[2] << 8)|brokenUpFrame[3];
              }
            }
          }
        }
      }
    }
    //Quad stroke
    if((flags & 8) == 8)
      if((flags & 8) == 8){
        drawLine(new IntWrapper(Math.round(vertices[0][0])), new IntWrapper(Math.round(vertices[0][1])), new IntWrapper(Math.round(vertices[1][0])), new IntWrapper(Math.round(vertices[1][1])), stroke);
        drawLine(new IntWrapper(Math.round(vertices[1][0])), new IntWrapper(Math.round(vertices[1][1])), new IntWrapper(Math.round(vertices[2][0])), new IntWrapper(Math.round(vertices[2][1])), stroke);
        drawLine(new IntWrapper(Math.round(vertices[2][0])), new IntWrapper(Math.round(vertices[2][1])), new IntWrapper(Math.round(vertices[3][0])), new IntWrapper(Math.round(vertices[3][1])), stroke);
        drawLine(new IntWrapper(Math.round(vertices[3][0])), new IntWrapper(Math.round(vertices[3][1])), new IntWrapper(Math.round(vertices[0][0])), new IntWrapper(Math.round(vertices[0][1])), stroke);
    }
  }
  
  
  public static void drawQuad(Quad sprite, byte compVal, char testType){
    tempAction = sprite.returnStencilActionPtr();

    if(sprite.getHasStroke()){
      stroke = sprite.returnStroke();
      flags|=8;
    }
    else
      flags&=-9;

    if(sprite.getHasFill()){
      fill = sprite.returnFill();
      flags|=16;
    }
    else
      flags&=-17;

    brokenUpFill[0] = fill >>> 24;
    boolean alwaysDraw = ((flags & -128) == 0) || (brokenUpFill[0] >= 255);
    int spriteWidth = sprite.returnImageWidth();
    int spriteHeight = sprite.returnImageHeight();
    char spriteMode = sprite.returnMode();
    float[][] vertexColours = sprite.returnVertexBrightness();
    float[][] vertices = sprite.getVertices();
    int[] texels = sprite.returnPixels();
    if(sprite.equalTransparencies()){
        brokenUpFill[0] = (int)(brokenUpFill[0]*vertexColours[0][0]);
    }
    if((flags & -128) == -128){
      brokenUpFill[0] = 255;
      stroke|=0xFF000000;
      vertexColours[0][0] = 1;
    }
    alphaNorm = brokenUpFill[0]*Colour.INV_255;
    brokenUpFill[1] = (fill >>> 16) & 0xFF;
    brokenUpFill[2] = (fill >>> 8) & 0xFF;
    brokenUpFill[3] = fill & 0xFF;


    
    vertices[0][2]*=(((flags & 4) >>> 1)-1);
    vertices[1][2]*=(((flags & 4) >>> 1)-1);
    vertices[2][2]*=(((flags & 4) >>> 1)-1);
    vertices[3][2]*=(((flags & 4) >>> 1)-1);
    //Determines if there is a self intersection between the sides
    boolean hasIntersection = hasIntersection(vertices[0][0], vertices[0][1], vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], false);
    hasIntersection|=hasIntersection(vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], vertices[0][0], vertices[0][1], false);
    if(hasIntersection){
      System.out.println("INTERSECTION");
      return;
    }
    float[] denominators = {(vertices[2][1] - vertices[3][1])*(vertices[0][0] - vertices[3][0]) + (vertices[3][0] - vertices[2][0])*(vertices[0][1] - vertices[3][1]),
                            (vertices[1][1] - vertices[2][1])*(vertices[0][0] - vertices[2][0]) + (vertices[2][0] - vertices[1][0])*(vertices[0][1] - vertices[2][1])};
    
    if(denominators[0] >= -EPSILON && denominators[0] <= EPSILON || denominators[1] >= -EPSILON && denominators[1] <= EPSILON){
      return;
    }
    
    //Quad fill
    if((flags & 16) == 16){
      float[] adjWeights = {1, 1, 1, 1};
      boolean useImage = sprite.hasImage();
      float[] intersect = {Float.NaN, Float.NaN};
      if(!sprite.isRectangle()){
        intersect = getIntersection(vertices[0][0], vertices[0][1], vertices[2][0], vertices[2][1], vertices[1][0], vertices[1][1], vertices[3][0], vertices[3][1]);
        if(!Float.isNaN(intersect[0]) && !Float.isNaN(intersect[1])){
          float[] dists = {(float)Math.sqrt((vertices[0][0]-intersect[0])*(vertices[0][0]-intersect[0]) + (vertices[0][1]-intersect[1])*(vertices[0][1]-intersect[1])),
                           (float)Math.sqrt((vertices[1][0]-intersect[0])*(vertices[1][0]-intersect[0]) + (vertices[1][1]-intersect[1])*(vertices[1][1]-intersect[1])),
                           (float)Math.sqrt((vertices[2][0]-intersect[0])*(vertices[2][0]-intersect[0]) + (vertices[2][1]-intersect[1])*(vertices[2][1]-intersect[1])),
                           (float)Math.sqrt((vertices[3][0]-intersect[0])*(vertices[3][0]-intersect[0]) + (vertices[3][1]-intersect[1])*(vertices[3][1]-intersect[1]))};

          if(dists[2] > EPSILON)
            adjWeights[0] = (dists[0]/dists[2] + 1);
          if(dists[3] > EPSILON)
            adjWeights[1] = (dists[1]/dists[3] + 1);
          if(dists[0] > EPSILON)
            adjWeights[2] = (dists[2]/dists[0] + 1);
          if(dists[1] > EPSILON)
            adjWeights[3] = (dists[3]/dists[1] + 1);
          }
          else{
            return;
          }
      }

      
      //Grabbing the x-boundries and y-boundries of the quadrilateral
      int[] xBounds = {Math.round(Math.max(0, Math.min(vertices[0][0], Math.min(vertices[1][0], Math.min(vertices[2][0], vertices[3][0]))))),
                       Math.round(Math.min(wid, Math.max(vertices[0][0], Math.max(vertices[1][0], Math.max(vertices[2][0], vertices[3][0])))))};
      int[] yBounds = {Math.round(Math.max(0, Math.min(vertices[0][1], Math.min(vertices[1][1], Math.min(vertices[2][1], vertices[3][1]))))),
                       Math.round(Math.min(heig, Math.max(vertices[0][1], Math.max(vertices[1][1], Math.max(vertices[2][1], vertices[3][1])))))};
      int minX = xBounds[0];
      int maxX = xBounds[1];
      denominators[0] = 1/denominators[0];
      denominators[1] = 1/denominators[1];
      float[] denominator = {vertices[1][1]-vertices[0][1]-EPSILON, 
                             vertices[2][1]-vertices[1][1]-EPSILON,
                             vertices[3][1]-vertices[2][1]-EPSILON,
                             vertices[0][1]-vertices[3][1]-EPSILON};

      if(denominator[0] < -EPSILON || denominator[0] > EPSILON)
        denominator[0] = 1/denominator[0];
      else
        denominator[0] = Float.NaN;
      if(denominator[1] < -EPSILON || denominator[1] > EPSILON)
        denominator[1] = 1/denominator[1];
      else
        denominator[1] = Float.NaN;
      if(denominator[2] < -EPSILON || denominator[2] > EPSILON)
        denominator[2] = 1/denominator[2];
      else
        denominator[2] = Float.NaN;
      if(denominator[3] < -EPSILON || denominator[3] > EPSILON)
        denominator[3] = 1/denominator[3];
      else
        denominator[3] = Float.NaN;
      float xPos = 0;
      float yPos = 0;
      int imgX = 0;
      int imgY = 0;
      int pixelPos = 0;
      int uvIndex = 0;
      float z = 0;
      float tempZ = 0;
      float u = 0;
      float v = 0;
      float adjustedAlpha = 0;
      float adjustedBeta = 0;
      float adjustedGamma = 0;
      float overallWeight = 0;

      int[] indices;
      float[] t = {-1, -1, -1, -1};

      //Stores the x-location of the interpolated edges
      float[] interpolatedEdges = {0, 0, 0, 0};
      for(int i = yBounds[0]; i < yBounds[1]; i++){
        //The current valid edge
        byte currentEdge = 1;
        //The centre of the pixel on the y-axis
        yPos = i+0.5f;
        //How far along the edge the scanline is
        
        //Computing all four t values
        t[0] = (vertices[1][1]-yPos)*denominator[0]-EPSILON;
        t[1] = (vertices[2][1]-yPos)*denominator[1]-EPSILON;
        t[2] = (vertices[3][1]-yPos)*denominator[2]-EPSILON;
        t[3] = (vertices[0][1]-yPos)*denominator[3]-EPSILON;
        
        //For when there are three or fewer valid edges (t is outside of the range of 0 and 1)
        //Computing the x-position of each edge if it is valid
        if(t[0] >= 0 && t[0] <= 1){
          interpolatedEdges[currentEdge] = (vertices[0][0]-vertices[1][0])*t[0]+vertices[1][0]-EPSILON;
          currentEdge--;
        }
        if(t[1] >= 0 && t[1] <= 1){
          interpolatedEdges[currentEdge] = (vertices[1][0]-vertices[2][0])*t[1]+vertices[2][0]-EPSILON;
          if(currentEdge > 0)
            currentEdge--;
          else
            currentEdge = 0;
        }
        if(t[2] >= 0 && t[2] <= 1){
          interpolatedEdges[currentEdge] = (vertices[2][0]-vertices[3][0])*t[2]+vertices[3][0]-EPSILON;
          if(currentEdge > 0)
            currentEdge--;
          else
            currentEdge = 0;
        }
        if(t[3] >= 0 && t[3] <= 1){
           interpolatedEdges[0] = (vertices[3][0]-vertices[0][0])*t[3]+vertices[0][0]-EPSILON;
        }
        //Finding the left-most edge and right most edge and locking them to be in between the left and right of the screen
        if(interpolatedEdges[0] <= interpolatedEdges[1]){
          xBounds[0] = Math.round(interpolatedEdges[0]);
          xBounds[1] = Math.round(interpolatedEdges[1]);
        }
        else{
          xBounds[0] = Math.round(interpolatedEdges[1]);
          xBounds[1] = Math.round(interpolatedEdges[0]);
        }
        if(xBounds[0] <= minX)
          xBounds[0] = minX;
        if(xBounds[1] >= maxX)
          xBounds[1] = maxX;

        //Drawing between the edges
        for(int j = xBounds[0]; j < xBounds[1]; j++){
          pixelPos = i*wid+j;
          stencilTest(pixelPos, compVal, testType);
          if((alwaysDraw || mesh[(i%meshSize)*meshSize+(j%meshSize)]) && (flags & 1) == 1 && (1 <= threshold || Math.random() < threshold)){
            xPos = j+0.5f; //The centre-x of the pixel
            boolean draw = true;//Determines if the current pixel should be updated
            indices = Quad.TRI_INDICES[0];
            uvIndex = 0;
            //Computing the weights of the current triangle
            alpha = ((vertices[indices[1]][1] - vertices[indices[2]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[2]][0] - vertices[indices[1]][0])*(yPos - vertices[indices[2]][1]))*denominators[0];
            beta = ((vertices[indices[2]][1] - vertices[indices[0]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[0]][0] - vertices[indices[2]][0])*(yPos - vertices[indices[2]][1]))*denominators[0];
            gamma = 1-alpha-beta;
            if(alpha < 0 || beta < 0 || gamma < 0){
              uvIndex = 1;
              indices = Quad.TRI_INDICES[1];
              alpha = ((vertices[indices[1]][1] - vertices[indices[2]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[2]][0] - vertices[indices[1]][0])*(yPos - vertices[indices[2]][1]))*denominators[1];
              beta = ((vertices[indices[2]][1] - vertices[indices[0]][1])*(xPos - vertices[indices[2]][0]) + (vertices[indices[0]][0] - vertices[indices[2]][0])*(yPos - vertices[indices[2]][1]))*denominators[1];
              gamma = 1-alpha-beta;
            }
  
            adjustedAlpha = adjWeights[indices[0]]*alpha;
            adjustedBeta = adjWeights[indices[1]]*beta;
            adjustedGamma = adjWeights[indices[2]]*gamma;
            if(useImage){
              overallWeight = alpha*adjWeights[indices[0]]+beta*adjWeights[indices[1]]+gamma*adjWeights[indices[2]];
              if(overallWeight > EPSILON)
                overallWeight = 1/overallWeight;
              else
                overallWeight = EPSILON;
              //Finding the UV coordinates of the texture in the current triangle
              //(Vertex A is guarenteed to be constant)
              //(For U, vertices B and C are both 1, though D can be 0)
              //(For V, vertices D and C are both 0, but B can be 1)
              u = (adjustedBeta+(uvIndex*adjustedGamma))*overallWeight;
              v = (adjustedAlpha+(uvIndex*adjustedBeta))*overallWeight;
              if(u >= 0 && u <= 1 && v >= 0 && v <= 1){
                //Converting from UV coordinates to the real coordinates in the image
                imgX = (int)(u*spriteWidth);
                imgY = (int)(v*spriteHeight);
                if(imgX >= spriteWidth)
                  imgX = spriteWidth-1;
                else if(imgX < 0)
                  imgX = 0;
                if(imgY >= spriteHeight)
                  imgY = spriteHeight-1;
                else if(imgY < 0)
                  imgY = 0;
                  
                //Grabbing the colour of the current pixel in the image
                draw = sprite.shouldDrawPixel(imgX, imgY);
                int spritePixel = texels[imgX+spriteWidth*imgY];
                //Breaking up the colours the image into their component RGB channels
          
                brokenUpSprite[0] = (spritePixel >>> 16) & 0xFF;
                brokenUpSprite[1] = (spritePixel >>> 8) & 0xFF;
                brokenUpSprite[2] = spritePixel & 0xFF;
                if(draw){
                  if(spriteMode == 'm' || spriteMode == 'u'){
                    //Multiplying the fill's channels by the image's channels
                    brokenUpColour[0] = (int)(brokenUpFill[0]);
                    brokenUpColour[1] = (int)(brokenUpSprite[0]*brokenUpFill[1]*Colour.INV_255); 
                    brokenUpColour[2] = (int)(brokenUpSprite[1]*brokenUpFill[2]*Colour.INV_255); 
                    brokenUpColour[3] = (int)(brokenUpSprite[2]*brokenUpFill[3]*Colour.INV_255);
                  }
                  else{
                    brokenUpColour[0] = 0xFF;
                    brokenUpColour[1] = brokenUpSprite[0];
                    brokenUpColour[2] = brokenUpSprite[1];
                    brokenUpColour[3] = brokenUpSprite[2];
                  }
                }
              }
            }

            if(!useImage || spriteMode == 'u' || spriteMode == 'k'){
              draw = true;
              brokenUpColour[0] = brokenUpFill[0];
              brokenUpColour[1] = brokenUpFill[1];
              brokenUpColour[2] = brokenUpFill[2];
              brokenUpColour[3] = brokenUpFill[3];
            }
            //Updating the pixel
            if(draw){
              int brokenUpFrame[] = {frame[pixelPos] >>> 24, (frame[pixelPos] >>> 16) & 0xFF, (frame[pixelPos] >>> 8) & 0xFF, frame[pixelPos] & 0xFF};
              z = vertices[indices[0]][2]*alpha+vertices[indices[1]][2]*beta+vertices[indices[2]][2]*gamma;
              adjustedAlpha*=vertices[indices[0]][2];
              adjustedBeta*=vertices[indices[1]][2];
              adjustedGamma*=vertices[indices[2]][2];
              tempZ = adjustedAlpha+adjustedBeta+adjustedGamma;
              if(tempZ < -EPSILON || tempZ > EPSILON)
                tempZ = 1/tempZ;
              else
                tempZ = EPSILON*(((flags & 4) >>> 4)-1);
              if(Float.isNaN(zBuff[pixelPos]) || ((flags & 4) == 0 && z <= zBuff[pixelPos] || zBuff[pixelPos] >= 0 && z > zBuff[pixelPos])){
                if(!sprite.equalTransparencies() && (flags & -128) == 0)
                  brokenUpColour[0] = Math.round(brokenUpColour[0]*tempZ*(vertexColours[indices[0]][0]*adjustedAlpha+vertexColours[indices[1]][0]*adjustedBeta+vertexColours[indices[2]][0]*adjustedGamma));
                else
                  brokenUpColour[0] = Math.round(brokenUpColour[0]*vertexColours[0][0]);
                if(brokenUpColour[0] >= 255)
                  brokenUpColour[0] = 255;
                else if(brokenUpColour[0] <= 0)
                  brokenUpColour[0] = 0;

                if(brokenUpColour[1] != 0 && brokenUpColour[2] != 0 && brokenUpColour[3] != 0){
                  if(!sprite.equalColour()){
                    float[] overallBrightness = {tempZ*(vertexColours[indices[0]][1]*adjustedAlpha+vertexColours[indices[1]][1]*adjustedBeta+vertexColours[indices[2]][1]*adjustedGamma),
                                                tempZ*(vertexColours[indices[0]][2]*adjustedAlpha+vertexColours[indices[1]][2]*adjustedBeta+vertexColours[indices[2]][2]*adjustedGamma),
                                                tempZ*(vertexColours[indices[0]][3]*adjustedAlpha+vertexColours[indices[1]][3]*adjustedBeta+vertexColours[indices[2]][3]*adjustedGamma)};
                    if(overallBrightness[0] <= 0)
                      overallBrightness[0] = 0;
                    if(overallBrightness[1] <= 0)
                      overallBrightness[1] = 0;
                    if(overallBrightness[2] <= 0)
                      overallBrightness[2] = 0;

                    brokenUpColour[1] = Math.round(brokenUpColour[1]*overallBrightness[0]);
                    brokenUpColour[2] = Math.round(brokenUpColour[2]*overallBrightness[1]);
                    brokenUpColour[3] = Math.round(brokenUpColour[3]*overallBrightness[2]);
                  }
                  else{
                    brokenUpColour[1] = Math.round(brokenUpColour[1]*vertexColours[0][1]);
                    brokenUpColour[2] = Math.round(brokenUpColour[2]*vertexColours[0][2]);
                    brokenUpColour[3] = Math.round(brokenUpColour[3]*vertexColours[0][3]);
                    if(brokenUpColour[1] <= 0)
                      brokenUpColour[1] = 0;
                    if(brokenUpColour[2] <= 0)
                      brokenUpColour[2] = 0;
                    if(brokenUpColour[3] <= 0)
                      brokenUpColour[3] = 0;
                  }
                  if(brokenUpColour[1] >= 255)
                    brokenUpColour[1] = 255;
                  if(brokenUpColour[2] >= 255)
                    brokenUpColour[2] = 255;
                  if(brokenUpColour[3] >= 255)
                    brokenUpColour[3] = 255;


                  }


                  if(brokenUpColour[0] < 0xFF)
                      Colour.interpolateColours(brokenUpColour, brokenUpFrame);
                  if(brokenUpColour[0] > minTransparency){
                    frame[pixelPos] = performStencilAction(brokenUpColour, j, i, compVal, pixelPos);       
                    zBuff[pixelPos] = z;
                  }
                }
                else if(brokenUpFrame[0] < 0xFF){
                  Colour.interpolateColours(brokenUpFrame, brokenUpColour);
                  if(brokenUpFrame[0] > minTransparency)
                    frame[pixelPos] = performStencilAction(brokenUpFrame, j, i, compVal, pixelPos);
                }
              }
            }
          }
        
      }
    }
    //Quad stroke
    if((flags & 8) == 8){
      drawLine(new IntWrapper(Math.round(vertices[0][0])), new IntWrapper(Math.round(vertices[0][1])), new IntWrapper(Math.round(vertices[1][0])), new IntWrapper(Math.round(vertices[1][1])), stroke);
      drawLine(new IntWrapper(Math.round(vertices[1][0])), new IntWrapper(Math.round(vertices[1][1])), new IntWrapper(Math.round(vertices[2][0])), new IntWrapper(Math.round(vertices[2][1])), stroke);
      drawLine(new IntWrapper(Math.round(vertices[2][0])), new IntWrapper(Math.round(vertices[2][1])), new IntWrapper(Math.round(vertices[3][0])), new IntWrapper(Math.round(vertices[3][1])), stroke);
      drawLine(new IntWrapper(Math.round(vertices[3][0])), new IntWrapper(Math.round(vertices[3][1])), new IntWrapper(Math.round(vertices[0][0])), new IntWrapper(Math.round(vertices[0][1])), stroke);
  
    }
  }
  
  
  //Tests if two lines are intersecting
  public static boolean hasIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, boolean countEnds){
      //Trying line-line intersection (https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection)
      //Found this through The Coding Train's 2D raycaster video
      float denominator = (x1 - x2)*(y3 - y4) - (x3 - x4)*(y1 - y2);
      if(denominator < -EPSILON || denominator > EPSILON){
        float t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4))/denominator;
        float u = -((x1-x2)*(y1-y3) - (y1-y2)*(x1-x3))/denominator;
        if(countEnds)
          return t >= 0 && t <= 1 && u >= 0 && u <= 1;
        else
          return t > EPSILON && t < (1-EPSILON) && u > EPSILON && u < (1-EPSILON);
      }
      return false;
  }
  //Locates the point of intersection between two lines
  public static float[] getIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
      //Trying line-line intersection (https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection)
      //Found this through The Coding Train's 2D raycaster video
      float[] output = {Float.NaN, Float.NaN};
      float denominator = (x1 - x2)*(y3 - y4) - (x3 - x4)*(y1 - y2);
      if(denominator < -EPSILON || denominator > EPSILON){
        float t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4))/denominator;
        if(t >= 0 && t <= 1){
          output[0] = x1+t*(x2 - x1);
          output[1] = y1+t*(y2-y1);
        }
      }
      return output;
  }
  
  
  public static void stencilTest(int pixelPos, byte compVal, char testType){
    flags|=1;
  }
  
  public static void setMinTransparency(int transparency){
    minTransparency = Math.max(0, Math.min(transparency, 255));
  }
  public static int getMinTransparency(){
    return minTransparency;
  }
  
    
  public static int performStencilAction(int[] colour, int x, int y, byte compVal, int pixelPos){
    tempAction.setColour(colour);
    tempAction.setPostion(x, y);
    tempAction.setComparison(compVal);
    tempAction.setStencilPixel(stencil[pixelPos]);
    tempAction.updateStencil();
    stencil[pixelPos] = tempAction.returnStencilValue();
    return (colour[0] << 24)|(colour[1] << 16)|(colour[2] << 8)|colour[3];
  }

  private static void interpLine(IntWrapper p1, IntWrapper p2, IntWrapper oldP1, IntWrapper oldP2, int oldOppP1, int oldOppP2, int farEdge){
    float t1 = -1;
    float t2 = -1;
    float denom = oldOppP2 - oldOppP1;
    if(denom < -EPSILON || denom > EPSILON){
      t1 = -oldOppP1/denom;
      t2 = (farEdge - 1 - oldOppP1)/denom;
      if(t1 >= 0 && t1 <= 1){
        if(oldOppP1 < 0)
          p1.val = Math.round((oldP2.val - oldP1.val)*t1 + oldP1.val);
        if(oldOppP2 < 0)
          p2.val = Math.round((oldP2.val - oldP1.val)*t1 + oldP1.val);
      }
      if(t2 >= 0 && t2 <= 1){
        if(oldOppP1 >= farEdge)
          p1.val = Math.round((oldP2.val - oldP1.val)*t2 + oldP1.val);
        if(oldOppP2 >= farEdge)
          p2.val = Math.round((oldP2.val - oldP1.val)*t2 + oldP1.val);
      }
    }
  }
  //Bresenham's line algorithm
  //https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
  public static void drawLine(IntWrapper x1, IntWrapper y1, IntWrapper x2, IntWrapper y2, int lineColour){
    int edgeDirX = x1.val < x2.val ? 1 : -1; //The direction of the line along the x-axis
    int edgeDirY = y1.val < y2.val ? 1 : -1; //The direction of the line along the y-axis
    int dx = Math.abs(x2.val-x1.val); //Difference between x2 and x1
    int dy = -Math.abs(y2.val-y1.val); //Difference between y2 and y1 (negated to account for how down is positive and up is negative)
    int error = dx+dy; //Sum of the differences between x2 and x1 and y2 and y1
    if((x1.val < 0 && x2.val < 0) || (y1.val < 0 && y2.val < 0) || (x1.val >= wid && x2.val >= wid) || (y1.val >= heig && y2.val >= heig))
      return;
    else if(!(Math.min(x1.val, x2.val) >= 0 && Math.max(x1.val, x2.val) < wid && Math.min(y1.val, y2.val) >= 0 && Math.max(y1.val, y2.val) < heig)){
      IntWrapper oldX1 = new IntWrapper(x1.val);
      IntWrapper oldX2 = new IntWrapper(x2.val);
      IntWrapper oldY1 = new IntWrapper(y1.val);
      IntWrapper oldY2 = new IntWrapper(y2.val);
      interpLine(y1, y2, oldY1, oldY2, oldX1.val, oldX2.val, wid);
      interpLine(x1, x2, oldX1, oldX2, oldY1.val, oldY2.val, heig);
  
      x1.val = Math.max(0, (Math.min(x1.val, wid-1)));
      x2.val = Math.max(0, (Math.min(x2.val, wid-1)));
      y1.val = Math.max(0, (Math.min(y1.val, heig-1)));
      y2.val = Math.max(0, (Math.min(y2.val, heig-1)));
    }
    
    while(true){
      //Interpolate between the line colour and the current pixel if the line colour's alpha channel is less than 255. Otherwise, simply overwrite the current pixel with the line colour
      if((lineColour >>> 24) < 0xFF){
        int pixelPos = wid*y1.val+x1.val;
        frame[pixelPos] = Colour.interpolateColours(lineColour, frame[pixelPos]);
      }
      else
        frame[wid*y1.val+x1.val] = lineColour;
      //Early break when the two points are the same
      if(x1.val == x2.val && y1.val == y2.val)
        break;
        
      final int error2 = error << 1; //Taking twice the error
      //For when twice the error is greater than the negative difference between y2 and y1
      if(error2 >= dy){
        //Verticle line
        if(x1.val == x2.val)
           break;
        error+=dy;
        x1.val+=edgeDirX;
      }
      //For when twice the error is less than the difference between x2 and x1
      if(error2 <= dx){
        //Horizontal line
        if(y1.val == y2.val)
          break;
        error+=dx;
        y1.val+=edgeDirY;
      }
    }
  }
}
