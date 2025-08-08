public abstract class FilledParent extends DispParent{
    //Flag bits: bit 1 = has stroke, bit 2 = has fill, bit 3 = equal transparencies, bit 4 = equal colour
    protected int fill = 0xFFFFFFFF;
    protected float maxFizzel = 1;
    protected float fizzelThreshold = 1.1f;
    protected StencilAction stencil = new StencilAction();

    public FilledParent(){
        super((byte)6);
        fill = 0xFFFFFFFF;
        maxFizzel = 1;
        fizzelThreshold = 1.1f;
        stencil = new StencilAction();
    }
    public FilledParent(byte newFlags){
        super(newFlags);
        fill = 0xFFFFFFFF;
        maxFizzel = 1;
        fizzelThreshold = 1.1f;
        stencil = new StencilAction();
    }
    public FilledParent(int newStroke, int newFill, boolean hasStroke, boolean hasFill){
        super(newStroke, (byte)(((hasStroke) ? 2 : 0)|((hasFill) ? 4 : 0)));
        if((newFill >>> 24) == 0){
            if(newFill <= 0xFF)
                fill = (0xFF000000) | (newFill << 16) | (newFill << 8) | newFill;
            else if(newFill <= 0xFF00)
                fill = ((newFill & 0xFF00) << 16) | ((newFill & 0xFF) << 16) | ((newFill & 0xFF) << 8) | (newFill & 0xFF);
            else
                fill = 0xFF000000 | newFill;
        }
        else
            fill = newFill;
        maxFizzel = 1;
        fizzelThreshold = 1.1f;
        stencil = new StencilAction();
    }


    public abstract void setVertices(float[][] newVertices);
    public abstract void setVertexBrightness(float r, float g, float b, byte index);
    public abstract void setVertexBrightness(float a, float r, float g, float b, byte index);
    public abstract void setVertexBrightness(float[] brightnessLevels, byte index);
    public abstract void setVertexBrightness(float[][] brightnessLevels);
    public abstract float getVertexPosition(byte vertex, byte axis);
    public abstract float[] returnVertexBrightness(byte index);
    public abstract float[][] returnVertexBrightness();
    public abstract float[][] getVertices();

    public void setStencilAction(StencilAction newAction){
        stencil = newAction;
    }

    public StencilAction returnStencilActionPtr(){
        return stencil;
    }

    //Stores the fill
    public void fill(int rgba){
        if((rgba >>> 24) == 0){
            if(rgba <= 0xFF)
                fill = (0xFF000000) | (rgba << 16) | (rgba << 8) | rgba;
            else if(rgba <= 0xFF00)
                fill = ((rgba & 0xFF00) << 16) | ((rgba & 0xFF) << 16) | ((rgba & 0xFF) << 8) | (rgba & 0xFF);
            else
                fill = 0xFF000000 | rgba;
        }
        else
            fill = rgba;
    }

    public void fill(int rgb, short alpha){
        fill&=0xFFFFFF;
        alpha&=0xFF;
        if(rgb <= 0xFF)
            fill = (rgb << 16) | (rgb << 8) | rgb;
        else
            fill = rgb;
        fill|=(alpha << 24);
    }

    public void fill(short r, short g, short b){
        r = (short)Math.min(Math.max(0, r), 0xFF);
        g = (short)Math.min(Math.max(0, g), 0xFF);
        b = (short)Math.min(Math.max(0, b), 0xFF);
        fill = 0xFF000000|(r << 16)|(g << 8)|b;
    }

    public void fill(short r, short g, short b, short alpha){
        r = (short)Math.min(Math.max(0, r), 0xFF);
        g = (short)Math.min(Math.max(0, g), 0xFF);
        b = (short)Math.min(Math.max(0, b), 0xFF);
        alpha = (short)Math.min(Math.max(0, alpha), 0xFF);
        fill = (alpha << 24)|(r << 16)|(g << 8)|b;
    }
    
    public void setAlphaFill(short alpha){
        fill&=0xFFFFFF;
        alpha&=0xFF;
        fill = (alpha << 24)|fill;
    }

    //Sets if the object has a stroke
    public void setHasStroke(boolean hasStroke){
        if(hasStroke)
            flags|=2;
        else
            flags&=-3;
    }

    //Sets if the object has a fill
    public void setHasFill(boolean hasFill){
        if(hasFill)
            flags|=4;
        else
            flags&=-5;
    }

    //Returns fill
    public int returnFill(){
        return fill; 
    }
    

    //Returns if the triangle has a stroke
    public boolean getHasStroke(){
        return (flags & 2) == 2;
    }
    
    //Returns if the triangle has a fill
    public boolean getHasFill(){
        return (flags & 4) == 4;
    }

    public boolean equalTransparencies(){
        return (flags & 8) == 8;
    }

    public boolean equalColour(){
        return (flags & 16) == 16;
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

    public boolean equals(Object o){
        if(o instanceof FilledParent){
            FilledParent f = (FilledParent)o;
            boolean isEquals = super.equals(f);
            isEquals&=(fill == f.fill);
            isEquals&=(Math.abs(maxFizzel - f.maxFizzel) <= EPSILON);
            isEquals&=(Math.abs(fizzelThreshold - f.fizzelThreshold) <= EPSILON);
            isEquals&=(stencil == f.stencil);
            return isEquals;
        }
        return false;
    }

    public boolean equals(FilledParent f){
        boolean isEquals = super.equals(f);
        isEquals&=(fill == f.fill);
        isEquals&=(Math.abs(maxFizzel - f.maxFizzel) <= EPSILON);
        isEquals&=(Math.abs(fizzelThreshold - f.fizzelThreshold) <= EPSILON);
        isEquals&=(stencil == f.stencil);
        return isEquals;
    }

    public void copy(Object o){
        if(o instanceof FilledParent){
            FilledParent f = (FilledParent)o;
            super.copy(f);
            fill = f.fill;
            maxFizzel = f.maxFizzel;
            fizzelThreshold = f.fizzelThreshold;
            stencil = f.stencil;
        }
    }

    public void copy(FilledParent f){
        super.copy(f);
        fill = f.fill;
        maxFizzel = f.maxFizzel;
        fizzelThreshold = f.fizzelThreshold;
        stencil = f.stencil;
    }
}