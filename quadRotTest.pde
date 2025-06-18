BillboardImg image;
float x11 = 15;
float x12 = 500;
float y11 = 500;
float y12 = 70;
  
float x21 = 50;
  
float y21 = 150;

int backgroundColour = 0xAAAAAA;
//float[][] vertices = {{0, 0}, {-100, -100}, {50, -50}, {100, -100}}; 
float[][] vertices = {{-100, -50, 1, 1}, {100, -50, 1, 1}, {100, 50, 1, 1}, {-100, 50, 1, 1}};
float[][] vertices2 = {{200, 200, 0.5, 1}, {400, 100, 5, 1}, {300, 300, 1.1, 1}, {200, 300, 0.1, 1}};
float angle = 0;
float scale = 1;
float angularVelocity = 0.01f;
Quad sprite;
Quad sprite2;
void setup(){
  size(800, 600);
  image = new BillboardImg("quadRotTest/testImage2.png");
  image.setInvisColour(0x00FF00, 0x00FF00);
  frameRate(30);
  sprite = new Quad(vertices, image, 0xFFFFFFFF, Colour.MAGENTA, true, false);
  float[][] vertexBrightness = {{3, 1, 0, 0}, {0.25, 0, 1, 0}, {0.25f, 0, 0, 1}, {3, 1, 1, 0}};
  sprite.setVertexBrightness(vertexBrightness);
  sprite.setStencilAction(new OtherTest());
  //sprite.setRemoval(true);
  sprite.setMode('m');
  sprite2 = new Quad(vertices2, image, Colour.WHITE, Colour.MAGENTA, true, false);
  sprite2.setStencilAction(new TestAction());
}

void draw(){
  float speed = 60/frameRate;
  background(backgroundColour);
  //float[] interpolatedEdges = {7, 5, 6, 2};
  //vertices[2][0] = mouseX;
  //float centreX = (vertices[0][0]+vertices[1][0]+vertices[2][0]+vertices[3][0])*0.25;
  //float centreY = (vertices[0][1]+vertices[1][1]+vertices[2][1]+vertices[3][1])*0.25;
  //vertices[2][1] = mouseY;
  
  if(keyPressed){
     if(key == '=' && scale < 5)
       scale+=0.01*speed;
     else if(key == '-' && scale > 0.01)
       scale-=0.01*speed;
     if(key == 's')
       angularVelocity = 0;
     else if(key == 'g')
       angularVelocity = 0.01f;
  }


  float[][] drawVertices = {{vertices[0][0], vertices[0][1], vertices[0][2], vertices[0][3]}, 
                            {vertices[1][0], vertices[1][1], vertices[1][2], vertices[1][3]},
                            {vertices[2][0], vertices[2][1], vertices[2][2], vertices[2][3]},
                            {vertices[3][0], vertices[3][1], vertices[3][2], vertices[3][3]}};
  angle+=angularVelocity*speed;
  for(byte i = 0; i < 4; i++){
   float tempX = drawVertices[i][0];
   float tempY = drawVertices[i][1];
   drawVertices[i][0] = (tempX*(float)Math.cos(angle)-tempY*(float)Math.sin(angle))*scale+mouseX;
   drawVertices[i][1] = (tempX*(float)Math.sin(angle)+tempY*(float)Math.cos(angle))*scale+mouseY;
  }
  
  stroke(#FF00FF);
  loadPixels();
  QuadDraw.setFrame(pixels, width, height);
  //QuadDraw.stroke(Colour.MAGENTA);
  //QuadDraw.fill(Colour.YELLOW);
  //QuadDraw.noStroke();


  
  QuadDraw.setProbabilities(sprite2.returnMaxFizzel(), sprite2.returnFizzelThreshold());
  QuadDraw.setDepthWrite(false);
  QuadDraw.drawQuad(sprite2, (byte)5, 'g');

  sprite.setVertices(drawVertices);
  //QuadDraw.noFill();
  QuadDraw.setDepthWrite(false);
  //QuadDraw.setProbabilities(5, 4.5);
  QuadDraw.drawQuad(sprite, (byte)5, 'g');

  //QuadDraw.drawLine(new IntWrapper(Math.round(x11)), new IntWrapper(Math.round(y11)), new IntWrapper(Math.round(x12)), new IntWrapper(Math.round(y12)), 0xFFFF00FF);
  //QuadDraw.drawLine(new IntWrapper(Math.round(x21)), new IntWrapper(Math.round(y21)), new IntWrapper(Math.round(x22)), new IntWrapper(Math.round(y22)), 0xFFFF00FF);
  updatePixels();
  System.out.println(frameRate);
  //if(QuadDraw.hasIntersection(vertices[2][0], vertices[2][1], vertices[1][0], vertices[1][1], vertices[3][0], vertices[3][1], vertices[0][0], vertices[0][1], false))
  //  backgroundColour = #FF777700;
  //else
  //  backgroundColour = #FF000000;
}

public class OtherTest extends StencilAction{
  public void updateStencil(){
    if(stencilPixel == -1){
      stencilPixel = (byte)(rgba[0] & 0x7F);
    }
    super.updateStencil();
  }
  
}

public class TestAction extends StencilAction{
  public void updateStencil(){
    stencilPixel = -1;
  }
}
