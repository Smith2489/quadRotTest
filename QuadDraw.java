public class QuadDraw{
  public static int fill = 0;
  public static int stroke = 0;
  public static byte flags = 2; //0 = hasStroke, 1 = hasFill
  public static int[] frame = new int[100000];
  public static int wid = 100;
  public static int heig = 100;
  public static void setFrame(int[] newFrame, int width, int height){
    frame = newFrame;
    wid = width;
    heig = height;
  }
  
  //Setting the fill of the current rect
  public static void fill(short r, short g, short b){
    flags|=16;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    fill = 0xFF000000|(r << 16)|(g << 8)|b;
  }
  public static void fill(short r, short g, short b, short a){
    flags|=16;
    r&=0xFF;
    g&=0xFF;
    b&=0xFF;
    a&=0xFF;
    fill = (a << 24)|(r << 16)|(g << 8)|b;
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
  }
  public static void fill(int colour, short alpha){
    flags|=16;
    alpha&=0xFF;
    colour = (colour & 0xFFFFFF);
    if(colour > 0xFF)
      fill = (alpha << 24)|colour;
    else
      fill = (alpha << 24)|(colour << 16)|(colour << 8)|colour;
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
  
  public static void drawQuad(Quad sprite){
    fill = sprite.returnFill();
    stroke = sprite.returnStroke();
    flags = (byte)(((sprite.hasStroke()) ? flags|8 : flags&-9));
    flags = (byte)(((sprite.hasFill()) ? flags|16 : flags&-17));
    float[][] vertices = {{sprite.returnVertices()[0][0], sprite.returnVertices()[0][1], sprite.returnVertices()[0][2]},
                          {sprite.returnVertices()[1][0], sprite.returnVertices()[1][1], sprite.returnVertices()[1][2]},
                          {sprite.returnVertices()[2][0], sprite.returnVertices()[2][1], sprite.returnVertices()[2][2]},
                          {sprite.returnVertices()[3][0], sprite.returnVertices()[3][1], sprite.returnVertices()[3][2]}};
    //Making sure there are four points in at least R^2 space
    if(vertices.length < 4){
      System.out.println("ERROR: TOO FEW VERTICES");
      return;
    }
    else if(vertices[0].length < 2){
       System.out.println("ERROR: TOO FEW DIMENSIONS");
       return;
    }
    //Determines if there is a self intersection between the sides
    boolean hasIntersection = hasIntersection(vertices[0][0], vertices[0][1], vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], false);
    hasIntersection|=hasIntersection(vertices[1][0], vertices[1][1], vertices[2][0], vertices[2][1], vertices[3][0], vertices[3][1], vertices[0][0], vertices[0][1], false);
    if(hasIntersection){
      System.out.println("INTERSECTION");
      return;
    }

    //Quad fill
    if((flags & 16) == 16){      
      
      
      float[] dists = new float[4];
      float[] adjWeights = {1, 1, 1, 1};
      boolean useImage = false;
      if(hasIntersection(vertices[0][0], vertices[0][1], vertices[2][0], vertices[2][1], vertices[1][0], vertices[1][1], vertices[3][0], vertices[3][1], true)){
        //triIndex = 0;
        useImage = sprite.hasImage();
        float[] intersect = getIntersection(vertices[0][0], vertices[0][1], vertices[2][0], vertices[2][1], vertices[1][0], vertices[1][1], vertices[3][0], vertices[3][1]);
        for(byte s = 0; s < 4; s++)
          dists[s] = (float)Math.sqrt((vertices[s][0]-intersect[0])*(vertices[s][0]-intersect[0]) + (vertices[s][1]-intersect[1])*(vertices[s][1]-intersect[1]));
        for(byte s = 0; s < 4; s++){
           adjWeights[s] = 0;
           if(dists[(s+2) & 3] > 0.0000001)
             adjWeights[s] = (dists[s]/dists[(s+2) & 3] + 1);
        }
      }
      
      float[][] centroids = {{vertices[Quad.TRI_INDICES[0][0]][0]+vertices[Quad.TRI_INDICES[0][1]][0]+vertices[Quad.TRI_INDICES[0][2]][0]*0.33333333333333333f,
                              vertices[Quad.TRI_INDICES[0][0]][1]+vertices[Quad.TRI_INDICES[0][1]][1]+vertices[Quad.TRI_INDICES[0][2]][1]*0.33333333333333333f},
                             {vertices[Quad.TRI_INDICES[1][0]][0]+vertices[Quad.TRI_INDICES[1][1]][0]+vertices[Quad.TRI_INDICES[1][2]][0]*0.33333333333333333f,
                              vertices[Quad.TRI_INDICES[1][0]][1]+vertices[Quad.TRI_INDICES[1][1]][1]+vertices[Quad.TRI_INDICES[1][2]][1]*0.33333333333333333f}};
      
      //Grabbing the x-boundries and y-boundries of the quadrilateral
      int[] xBounds = {Math.round(Math.max(0, Math.min(vertices[0][0], Math.min(vertices[1][0], Math.min(vertices[2][0], vertices[3][0]))))),
                       Math.round(Math.min(wid, Math.max(vertices[0][0], Math.max(vertices[1][0], Math.max(vertices[2][0], vertices[3][0])))))};
      int[] yBounds = {Math.round(Math.max(0, Math.min(vertices[0][1], Math.min(vertices[1][1], Math.min(vertices[2][1], vertices[3][1]))))),
                       Math.round(Math.min(heig, Math.max(vertices[0][1], Math.max(vertices[1][1], Math.max(vertices[2][1], vertices[3][1])))))};
      int minX = xBounds[0];
      int maxX = xBounds[1];
      //Stores the x-location of the interpolated edges
      float[] interpolatedEdges = {0, 0, 0, 0};
      for(int i = yBounds[0]; i < yBounds[1]; i++){
        //The current valid edge
        byte currentEdge = 1;
        //The centre of the pixel on the y-axis
        float yPos = i+0.5f;
        //How far along the edge the scanline is
        float[] t = {-1, -1, -1, -1};
        
        //Computing all four t values
        for(byte j = 0; j < 4; j++){
          float denominator = vertices[(j+1)&3][1]-vertices[j][1]-0.0000001f;
          if(Math.abs(denominator) > 0.0000001f)
            t[j] = (vertices[(j+1)&3][1]-yPos)/denominator-0.0000001f;
        }
        
        if(!(t[0] >= 0 && t[0] <= 1 && t[1] >= 0 && t[1] <= 1 && t[2] >= 0 && t[2] <= 1 && t[3] >= 0 && t[3] <= 1)){
          //For when there are three or fewer valid edges (t is outside of the range of 0 and 1)
          //Computing the x-position of each edge if it is valid
          if(t[0] >= 0 && t[0] <= 1){
            interpolatedEdges[currentEdge] = (vertices[0][0]-vertices[1][0])*t[0]+vertices[1][0]-0.0000001f;
            currentEdge--;
          }
          if(t[1] >= 0 && t[1] <= 1){
            interpolatedEdges[currentEdge] = (vertices[1][0]-vertices[2][0])*t[1]+vertices[2][0]-0.0000001f;
            currentEdge = (byte)((currentEdge <= 0) ? 0 : currentEdge-1);
          }
          if(t[2] >= 0 && t[2] <= 1){
            interpolatedEdges[currentEdge] = (vertices[2][0]-vertices[3][0])*t[2]+vertices[3][0]-0.0000001f;
            currentEdge = (byte)((currentEdge <= 0) ? 0 : currentEdge-1);
          }
          if(t[3] >= 0 && t[3] <= 1){
            interpolatedEdges[0] = (vertices[3][0]-vertices[0][0])*t[3]+vertices[0][0]-0.0000001f;
          }
          //Finding the left-most edge and right most edge and locking them to be in between the left and right of the screen
          xBounds[0] = Math.round(Math.max(minX, Math.min(interpolatedEdges[0], interpolatedEdges[1])));
          xBounds[1] = Math.round(Math.min(maxX, Math.max(interpolatedEdges[0], interpolatedEdges[1])));
          updatePixels(vertices, sprite, xBounds, (byte)0, i, yPos, adjWeights, centroids, useImage);
        }
        else{
           //For when all edges are valid (in the range of 0 and 1)
           //Computing the x-position of each edge
           interpolatedEdges[0] = (vertices[0][0]-vertices[1][0])*t[0]+vertices[1][0]-0.00001f;
           interpolatedEdges[1] = (vertices[1][0]-vertices[2][0])*t[1]+vertices[2][0]-0.00001f;
           interpolatedEdges[2] = (vertices[2][0]-vertices[3][0])*t[2]+vertices[3][0]-0.00001f;
           interpolatedEdges[3] = (vertices[3][0]-vertices[0][0])*t[3]+vertices[0][0]-0.00001f;
           //Sorting the edges to be in left-to-right order using insertion sort
           for(byte j = 1; j < 4; j++){
             for(byte s = j; s > 0; s--){
               float tempEdge = interpolatedEdges[s-1];
               if(tempEdge >= interpolatedEdges[s]){
                 interpolatedEdges[s-1] = interpolatedEdges[s];
                 interpolatedEdges[s] = tempEdge;
               }
             }
           }
           //Restricting the edges to be within the screen
           interpolatedEdges[0] = Math.max(0, Math.min(interpolatedEdges[0], wid));
           interpolatedEdges[1] = Math.max(0, Math.min(interpolatedEdges[1], wid));
           interpolatedEdges[2] = Math.max(0, Math.min(interpolatedEdges[2], wid));
           interpolatedEdges[3] = Math.max(0, Math.min(interpolatedEdges[3], wid));
           //Drawing from the left-most to second left-most edge
           xBounds[0] = Math.round(interpolatedEdges[0]);
           xBounds[1] = Math.round(interpolatedEdges[1]);
           for(int j = xBounds[0]; j < xBounds[1]; j++){
            if((fill >>> 24) < 0xFF)
              frame[i*wid+j] = Colour.interpolateColours(fill, frame[i*wid+j]); 
            else
              frame[i*wid+j] = fill;
           }
           //Drawing from the second right-most to the right-most edge
           xBounds[0] = Math.round(interpolatedEdges[2]);
           xBounds[1] = Math.round(interpolatedEdges[3]);
           for(int j = xBounds[0]; j < xBounds[1]; j++){
             if((fill >>> 24) < 0xFF)
               frame[i*wid+j] = Colour.interpolateColours(fill, frame[i*wid+j]); 
             else
               frame[i*wid+j] = fill;
           }
        }
      }
    }
    //Quad stroke
    if((flags & 8) == 8)
      for(byte i = 0; i < 4; i++)
        drawLine(new IntWrapper(Math.round(vertices[i][0])), new IntWrapper(Math.round(vertices[i][1])), new IntWrapper(Math.round(vertices[(i+1)&3][0])), new IntWrapper(Math.round(vertices[(i+1)&3][1])), stroke);
  }
  
  
  private static void updatePixels(float[][] vertices, Quad sprite, int[] xBounds, byte triIndex, int i, float yPos, float[] adjWeights, float[][] centroids, boolean useImage){
    //Drawing between the edges
    for(int j = xBounds[0]; j < xBounds[1]; j++){
      float xPos = j+0.5f; //The centre-x of the pixel
      int colour = fill; //Temporarily storing the fill as a separate colour
      boolean draw = true;//Determines if the current pixel should be updated
      int[] indices = Quad.TRI_INDICES[triIndex];
      
      
      //Calculates the distance from the current pixel to the centroids of each triangle to figure out which side of the quadrilateral's diagonal its on
      float dist1 = (centroids[0][0] - xPos)*(centroids[0][0] - xPos) + (centroids[0][1] - yPos)*(centroids[0][1] - yPos);
      float dist2 = (centroids[1][0] - xPos)*(centroids[1][0] - xPos) + (centroids[1][1] - yPos)*(centroids[1][1] - yPos);
      if(dist2 < dist1)
        indices = Quad.TRI_INDICES[triIndex+1];
      
      //Computing the weights of the current triangle
      float alpha = returnAlpha(vertices[indices[0]][0], vertices[indices[0]][1], 
                                vertices[indices[1]][0], vertices[indices[1]][1], 
                                vertices[indices[2]][0], vertices[indices[2]][1], 
                                xPos, yPos); 
      float beta = returnBeta(vertices[indices[0]][0], vertices[indices[0]][1], 
                              vertices[indices[1]][0], vertices[indices[1]][1], 
                              vertices[indices[2]][0], vertices[indices[2]][1], 
                              xPos, yPos); 
      float gamma = returnGamma(alpha, beta);
      if(useImage){
        float overallWeight = alpha*adjWeights[indices[0]]+beta*adjWeights[indices[1]]+gamma*adjWeights[indices[2]];
        //Finding the UV coordinates of the texture in the current triangle
        float u = ((Quad.UV_COORDS[indices[0]][0]*adjWeights[indices[0]]*alpha)+(Quad.UV_COORDS[indices[1]][0]*adjWeights[indices[1]]*beta)+(Quad.UV_COORDS[indices[2]][0]*adjWeights[indices[2]]*gamma))/overallWeight;
        float v = ((Quad.UV_COORDS[indices[0]][1]*adjWeights[indices[0]]*alpha)+(Quad.UV_COORDS[indices[1]][1]*adjWeights[indices[1]]*beta)+(Quad.UV_COORDS[indices[2]][1]*adjWeights[indices[2]]*gamma))/overallWeight;
        u = Math.abs(u - (int)(u));
        v = Math.abs(v - (int)(v));

        //Converting from UV coordinates to the real coordinates in the image
        int imgX = Math.round(u*sprite.returnImageWidth()-0.5f);
        int imgY = Math.round(v*sprite.returnImageHeight()-0.5f);
        //Grabbing the colour of the current pixel in the image

        draw = sprite.shouldDrawPixel(imgX+sprite.returnImageWidth()*imgY) || sprite.hasRemoval();
        if(draw){
           int spritePixel = sprite.returnPixels()[imgX+sprite.returnImageWidth()*imgY];
          if(sprite.returnMode() == 'm' || sprite.returnMode() == 'u'){
            //Breaking up the colours in the fill and the image into their component ARGB values
            int[] brokenUpFill = {fill & 0xFF000000, ((fill >>> 16) & 0xFF), ((fill >>> 8) & 0xFF), fill & 0xFF};
            int[] brokenUpSprite = {0xFF000000, ((spritePixel >>> 16) & 0xFF), ((spritePixel >>> 8) & 0xFF), spritePixel & 0xFF};
  
            //Multiplying the fill's channels by the image's channels and recombining them
            int[] brokenUpColour = {brokenUpFill[0], (int)(brokenUpSprite[1]*brokenUpFill[1]*0.003921569f), (int)(brokenUpSprite[2]*brokenUpFill[2]*0.003921569f), (int)(brokenUpSprite[3]*brokenUpFill[3]*0.003921569f)};
            colour = brokenUpColour[0]|(brokenUpColour[1] << 16)|(brokenUpColour[2] << 8) | brokenUpColour[3];
           }
           else
              colour = (fill & 0xFF000000) | (spritePixel & 0xFFFFFF);
         }
         if(!draw && (sprite.returnMode() == 'u' || sprite.returnMode() == 'k')){
           draw = true;
           colour = fill;
         }
       }
       //Updating the pixels
       if(draw){
         if((fill >>> 24) < 0xFF)
           frame[i*wid+j] = Colour.interpolateColours(colour, frame[i*wid+j]); 
         else
           frame[i*wid+j] = colour;
      }
    }
  }
  
  
  
  //Tests if two lines are intersecting
  public static boolean hasIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, boolean countEnds){
      //Trying line-line intersection (https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection)
      //Found this through The Coding Train's 2D raycaster video
      float denominator = (x1 - x2)*(y3 - y4) - (x3 - x4)*(y1 - y2);
      if(Math.abs(denominator) > 0.0001){
        float t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4))/denominator;
        float u = -((x1-x2)*(y1-y3) - (y1-y2)*(x1-x3))/denominator;
        if(countEnds)
          return t >= 0 && t <= 1 && u >= 0 && u <= 1;
        else
          return t > 0.0001 && t < 0.9999 && u > 0.0001 && u < 0.9999;
      }
      return false;
  }
    //Tests if two lines are intersecting
  public static float[] getIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
      //Trying line-line intersection (https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection)
      //Found this through The Coding Train's 2D raycaster video
      float[] output = {Float.NaN, Float.NaN};
      float denominator = (x1 - x2)*(y3 - y4) - (x3 - x4)*(y1 - y2);
      if(Math.abs(denominator) > 0.0001){
        float t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4))/denominator;
        float u = -((x1-x2)*(y1-y3) - (y1-y2)*(x1-x3))/denominator;
        if(t >= 0 && t <= 1 && u >= 0 && u <= 1){
          output[0] = x1+t*(x2 - x1);
          output[1] = y1+t*(y2-y1);
        }
      }
      return output;
  }
  
  //Baarycentric coordinates
  //Weight contributed by the first vertex
  public static float returnAlpha(float x1, float y1, float x2, float y2, float x3, float y3, float pX, float pY){
    float denominator = (y2 - y3)*(x1 - x3) + (x3 - x2)*(y1 - y3);
    if(Math.abs(denominator) <= 0.00000001){
      System.out.println("ERROR: DIV BY 0");
      System.exit(1);
    }
    float numerator = (y2 - y3)*(pX - x3) + (x3 - x2)*(pY - y3);
    return numerator/denominator;
  }
  //Weight contributed by the second vertex
  public static float returnBeta(float x1, float y1, float x2, float y2, float x3, float y3, float pX, float pY){
    float denominator = (y2 - y3)*(x1 - x3) + (x3 - x2)*(y1 - y3);
    if(Math.abs(denominator) <= 0.000000001){
      System.out.println("ERROR: DIV BY 0");
      System.exit(1);
    }
    float numerator = (y3 - y1)*(pX - x3) + (x1 - x3)*(pY - y3);
    return numerator/denominator;
  }
  //Weight contributed by the third vertex (exploits how Barycentric coordinates all add up to one)
  public static float returnGamma(float alpha, float beta){
     return 1-alpha-beta; 
  }
  
  private static void interpLine(IntWrapper p1, IntWrapper p2, IntWrapper oldP1, IntWrapper oldP2, int oldOppP1, int oldOppP2, int farEdge){
    float t1 = -1;
    float t2 = -1;
    float denom = oldOppP2 - oldOppP1;
    if(Math.abs(denom) > 0.0001){
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
