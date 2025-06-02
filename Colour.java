//A class taht literally just store a bunch of predefined colour values and functions to compute different colours
public class Colour {
    //Credit for these hex values goes to Rapid Table and their RGB to hex page
    //https://www.rapidtables.com/convert/color/rgb-to-hex.html
    //(Accessed on 18.1.2025)
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int RED = 0xFFFF0000;
    public static final int LIME = 0xFF00FF00;
    public static final int BLUE = 0xFF0000FF;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int CYAN = 0xFF00FFFF;
    public static final int MAGENTA = 0xFFFF00FF;
    public static final int SILVER = 0xFFC0C0C0;
    public static final int GREY = 0xFF808080;
    public static final int MAROON = 0xFF800000;
    public static final int OLIVE = 0xFF808000;
    public static final int GREEN = 0xFF008000;
    public static final int PURPLE = 0xFF800080;
    public static final int TEAL = 0xFF008080;
    public static final int NAVY = 0xFF000080;

    //Credit for these goes to HTML Color Codes's pages on their respective colours
    //The orange page: https://htmlcolorcodes.com/colors/shades-of-orange/
    //Accessed on 19.1.2024
    public static final int ORANGE = 0xFFFF5733;
    public static final int AMBER = 0xFFFFBF00;
    public static final int BRIGHT_ORANGE = 0xFFFFAC1C;
    public static final int GOLDEN_YELLOW = 0xFFFFC000;
    public static final int MANGO = 0xFFF4BB44;
    public static final int TERRA_COTTA = 0xFFE3735E;
    //The pink section of the HTML colour names page https://htmlcolorcodes.com/color-names/
    //Accessed on 19.1.2024
    public static final int PINK = 0xFFFFC0CB;
    public static final int LIGHT_PINK = 0xFFFFB6C1;
    public static final int DEEP_PINK = 0xFFFF1493;
    //The purple section of the same page
    //Accessed on 19.1.2024
    public static final int LAVANDER = 0xFFE6E6FA;
    //The green section
    //Accessed on 19.1.2024
    public static final int DARK_SEA_GREEN = 0xFF8FBC8B;
    public static final int LIGHT_SEA_GREEN = 0xFF20B2AA;
    //The brown section
    //Accessed on 19.1.2024
    public static final int BROWN = 0xFFA52A2A;
    public static final int CHOCOLATE = 0xFFD2691E;
    //The white section
    //Accessed on 19.1.2024
    public static final int FLORAL_WHITE = 0xFFFFFAF0;
    public static final int LAVANDER_BLUSH = 0xFFFFF0F5;
    public static final int MISTY_ROSE = 0xFFFFE4E1;

    //A set of functions which return a custom colour
    public static int colour(int rgba){
        if((rgba >>> 24) == 0){
            if(rgba <= 0xFF)
                return 0xFF000000 | (rgba << 16) | (rgba << 8) | rgba;
            else if(rgba <= 0xFFFF)
                return ((rgba & 0xFF00) << 16) | ((rgba & 0xFF) << 16) | ((rgba & 0xFF) << 8) | (rgba & 0xFF);
            else
                return 0xFF000000 | rgba;
        }
        else
            return rgba;
    }
    public static int colour(int rgb, int alpha){
        rgb&=0xFFFFFF;
        alpha = (alpha & 0xFF) << 24;
        if(rgb <= 0xFF)
            return alpha | (rgb << 16) | (rgb << 8) | rgb;
        else
            return alpha | rgb;
    }
    public static int colour(int red, int green, int blue){
        red = (red & 0xFF) << 16;
        green = (green & 0xFF) << 8;
        blue = blue & 0xFF;
        return 0xFF000000 | red | green | blue;
    }
    public static int colour(int red, int green, int blue, int alpha){
        alpha = (alpha & 0xFF) << 24;
        red = (red & 0xFF) << 16;
        green = (green & 0xFF) << 8;
        blue = blue & 0xFF;
        return alpha | red | green | blue;
    }
    //Computes the luminance of a colour assuming an SRGB space
    public static float computeLuminance(int colour){
        //Normalizes the red, green, and blue channels
        float r = ((colour >>> 16) & 0xFF)*0.003921569f;
        float g = ((colour >>> 8) & 0xFF)*0.003921569f;
        float b = (colour & 0xFF)*0.003921569f;
        //Converts the channels to linear channels
        if(r <= 0.04045)
            r*=0.07739938f;
        else
            r = (float)(Math.pow((r+0.055f)*0.947867298, 2.4));
        if(g <= 0.04045)
            g*=0.07739938f;
        else
            g = (float)(Math.pow((g+0.055f)*0.947867298, 2.4));
        if(b <= 0.04045)
            b*=0.07739938f;
        else
            b = (float)(Math.pow((b+0.055f)*0.947867298, 2.4));
        //Multiplies the linearized channels by a set of weights
        return r*0.2126f+g*0.7152f+b*0.0722f;
    }
    public static float computeLuminance(short red, short green, short blue){
        //Normalizes the red, green, and blue channels
        float r = (red & 0xFF)*0.003921569f;
        float g = (green & 0xFF)*0.003921569f;
        float b = (blue & 0xFF)*0.003921569f;
        //Converts the channels to linear channels
        if(r <= 0.04045)
            r*=0.07739938f;
        else
            r = (float)(Math.pow((r+0.055f)*0.947867298, 2.4));
        if(g <= 0.04045)
            g*=0.07739938f;
        else
            g = (float)(Math.pow((g+0.055f)*0.947867298, 2.4));
        if(b <= 0.04045)
            b*=0.07739938f;
        else
            b = (float)(Math.pow((b+0.055f)*0.947867298, 2.4));
        //Multiplies the linearized channels by a set of weights
        return r*0.2126f+g*0.7152f+b*0.0722f;
    }
    //Computes the average of two colours using an external alpha channel
    public static int interpolateColours(int colourA, int colourB, float alpha){
        //Extracting the individual channels' data for pixel A and pixel B
        alpha = Math.max(0, Math.min(alpha, 1));
        int[][] channels = {{colourA >>> 24, colourB >>> 24}, {(colourA >>> 16) & 0xFF, (colourB >>> 16) & 0xFF}, {(colourA >>> 8) & 0xFF, (colourB >>> 8) & 0xFF}, {colourA & 0xFF, colourB & 0xFF}};
        int[] returnChannels = {0, 0, 0, 0};
        //Linearly interpolating between pixel B's channels and pixel A's channels
        for(byte i = 0; i < 4; i++)
          returnChannels[i] = (int)((channels[i][0]-channels[i][1])*alpha+channels[i][1]) << ((3-i) << 3);
    
        //Combining the results
        return returnChannels[0]|returnChannels[1]|returnChannels[2]|returnChannels[3];
    }
    //Computes the average of two colours using colour A's alpha channel
    public static int interpolateColours(int colourA, int colourB){
        //Computing t and 1-t for pixelA
        float alpha = (colourA >>> 24)*0.003921569f;
        //Extracting the individual channels' data for pixel A and pixel B
        int[][] channels = {{colourA >>> 24, colourB >>> 24}, {(colourA >>> 16) & 0xFF, (colourB >>> 16) & 0xFF}, {(colourA >>> 8) & 0xFF, (colourB >>> 8) & 0xFF}, {colourA & 0xFF, colourB & 0xFF}};
        int[] returnChannels = {0, 0, 0, 0};
        //Linearly interpolating between pixel B's channels and pixel A's channels
        for(byte i = 0; i < 4; i++)
            returnChannels[i] = (int)((channels[i][0]-channels[i][1])*alpha+channels[i][1]) << ((3-i) << 3);

        //Combining the results
        return returnChannels[0]|returnChannels[1]|returnChannels[2]|returnChannels[3];
    }
    public static int interpolateColours(int[] pixelA, int[] pixelB){
      float alphaNorm = pixelA[0]*0.003921569f;
      int[] colour = {(int)((pixelA[0] - pixelB[0])*alphaNorm + pixelB[0]) << 24,
                      (int)((pixelA[1] - pixelB[1])*alphaNorm + pixelB[1]) << 16,
                      (int)((pixelA[2] - pixelB[2])*alphaNorm + pixelB[2]) << 8,
                      (int)((pixelA[3] - pixelB[3])*alphaNorm + pixelB[3])};
      return colour[0]|colour[1]|colour[2]|colour[3];
  }
  public static int interpolateColours(int[] pixelA, int[] pixelB, float alphaNorm){
      int[] colour = {(int)((pixelA[0] - pixelB[0])*alphaNorm + pixelB[0]) << 24,
                      (int)((pixelA[1] - pixelB[1])*alphaNorm + pixelB[1]) << 16,
                      (int)((pixelA[2] - pixelB[2])*alphaNorm + pixelB[2]) << 8,
                      (int)((pixelA[3] - pixelB[3])*alphaNorm + pixelB[3])};
      return colour[0]|colour[1]|colour[2]|colour[3];
  }
}
