import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
public class BillboardImg {
    private int[] img = new int[10000];
    private int width = 100;
    private int height = 100;
    private int[] removalColour = {0xFF00FF, 0xFF00FF}; //Pixels of this colour will be skipped
    public BillboardImg(){
        width = 100;
        height = 100;
        removalColour[0] = 0xFF00FF;
        removalColour[1] = 0xFF00FF;
        img = new int[10000];
        for(int i = 0; i < 10000; i++)
            img[i] = 0xFF000000;
    }
    public BillboardImg(String imagePath){
        File file = new File(imagePath);
        try{
          BufferedImage sprite = ImageIO.read(file);
          width = sprite.getWidth();
          height = sprite.getHeight();
          img = new int[width*height];
          for(int i = 0; i < width; i++)
            for(int j = 0; j < height; j++)
              img[j*width+i] = sprite.getRGB(i, j);
        }
        catch(Exception e){
          System.out.println("ERROR: FILE "+imagePath+" NOT FOUND OR IS INVALID");
          System.exit(1);
        }
        removalColour[0] = 0xFF00FF;
        removalColour[1] = 0xFF00FF;
      }
      public BillboardImg(int[] newImage, int newWidth, int newHeight){
        if(newWidth*newHeight != newImage.length){
            System.out.println("ERROR: DIMENSIONS DO NOT MATCH");
            System.exit(1);
        }
        img = new int[newImage.length];
        width = newWidth;
        height = newHeight;
        for(int i = 0; i < newImage.length; i++)
          img[i] = newImage[i];
        removalColour[0] = 0xFF00FF;
        removalColour[1] = 0xFF00FF;
      }
      public void setImage(String imagePath){
        File file = new File(imagePath);
        try{
          BufferedImage sprite = ImageIO.read(file);
          width = sprite.getWidth();
          height = sprite.getHeight();
          img = new int[width*height];
          for(int i = 0; i < width; i++)
            for(int j = 0; j < height; j++)
              img[j*width+i] = sprite.getRGB(i, j);
        }
        catch(Exception e){
          System.out.println("ERROR: FILE "+imagePath+" NOT FOUND OR IS INVALID");
          System.exit(1);
        }
      }
      public void setImage(int[] newImage, int newWidth, int newHeight){
        if(newWidth*newHeight != newImage.length){
            System.out.println("ERROR: DIMENSIONS DO NOT MATCH");
            System.exit(1);
        }
        img = new int[newImage.length];
        width = newWidth;
        height = newHeight;
        for(int i = 0; i < newImage.length; i++)
          img[i] = newImage[i];
      }
      public void setInvisColour(int invisColour1, int invisColour2){
        invisColour1&=0xFFFFFF;
        if(invisColour1 > 0xFF)
          removalColour[0] = invisColour1;
        else
          removalColour[0] = (invisColour1 << 16) | (invisColour1 << 8) | invisColour1;
        invisColour2&=0xFFFFFF;
        if(invisColour2 > 0xFF)
          removalColour[1] = invisColour2;
        else
          removalColour[1] = (invisColour2 << 16) | (invisColour2 << 8) | invisColour2;
      }
      public void setInvisColour(byte r, byte g, byte b, byte index){
        removalColour[index] = ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
      }
      
      public int[] returnPixels(){
        return img;
      }
      public int returnWidth(){
        return width;
      }
      public int returnHeight(){
        return height;
      }
      public int returnInvisColour(byte index){
        return removalColour[index];
      }
      public boolean shouldDrawPixel(int pixelIndex){
        int[] tempR = {(removalColour[0] >>> 16) & 0xFF, (removalColour[1] >>> 16) & 0xFF, (img[pixelIndex] >>> 16) & 0xFF};
        int[] edgeR = {Math.min(tempR[0], tempR[1]), Math.max(tempR[0], tempR[1])};
        int[] tempG = {(removalColour[0] >>> 8) & 0xFF, (removalColour[1] >>> 8) & 0xFF, (img[pixelIndex] >>> 8) & 0xFF};
        int[] edgeG = {Math.min(tempG[0], tempG[1]), Math.max(tempG[0], tempG[1])};
        int[] tempB = {removalColour[0] & 0xFF, removalColour[1] & 0xFF, img[pixelIndex] & 0xFF};
        int[] edgeB = {Math.min(tempB[0], tempB[1]), Math.max(tempB[0], tempB[1])};
        boolean shouldDraw = tempR[2] >= edgeR[0] && tempR[2] <= edgeR[1];
        shouldDraw&=(tempG[2] >= edgeG[0] && tempG[2] <= edgeG[1]);
        shouldDraw&=(tempB[2] >= edgeB[0] && tempB[2] <= edgeB[1]);
        return !shouldDraw;
      }

      public boolean equals(Object o){
        if(o instanceof BillboardImg){
          BillboardImg b = (BillboardImg)o;
          boolean isEqual = true;
          isEqual&=(width == b.width);
          isEqual&=(height == b.height);
          isEqual&=(removalColour == b.removalColour);
          isEqual&=(img.length == b.img.length);
          if(isEqual)
            for(int i = 0; i < img.length; i++){
              if(img[i] != b.img[i])
                return false;
            }
          return isEqual;
        }
        else
          return false;
      }
      public boolean equals(BillboardImg b){
        boolean isEqual = true;
        isEqual&=(width == b.width);
        isEqual&=(height == b.height);
        isEqual&=(removalColour == b.removalColour);
        isEqual&=(img.length == b.img.length);
        if(isEqual)
          for(int i = 0; i < img.length; i++){
            if(img[i] != b.img[i])
              return false;
          }
        return isEqual;
      }
      public void copy(Object o){
        if(o instanceof BillboardImg){
          BillboardImg b = (BillboardImg)o;
          width = b.width;
          height = b.height;
          removalColour = b.removalColour;
          img = new int[b.img.length];
          for(int i = 0; i < img.length; i++)
            img[i] = b.img[i];
        }
      }
      public void copy(BillboardImg b){
        width = b.width;
        height = b.height;
        removalColour = b.removalColour;
        img = new int[b.img.length];
        for(int i = 0; i < img.length; i++)
          img[i] = b.img[i];
      }
}
