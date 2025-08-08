public abstract class DispParent{
    protected static final float EPSILON = 0.0000001f;
    protected byte flags = 0; //Bit 0 = depthWrite
    protected int stroke = 0xFF000000;

    public abstract float[] returnPosition();
    public abstract float returnX();
    public abstract float returnY();
    public abstract float returnZ();

    public DispParent(){
        flags = 0;
        stroke = 0xFF000000;
    }
    public DispParent(byte newFlags){
        flags = newFlags;
        stroke = 0xFF000000;
    }

    public DispParent(int rgba){
        flags = 0;
        if((rgba >>> 24) == 0){
            if(rgba <= 0xFF)
                stroke = 0xFF000000 | (rgba << 16) | (rgba << 8) | rgba;
            else if(rgba <= 0xFF00)
                stroke = ((rgba & 0xFF00) << 16) | ((rgba & 0xFF) << 16) | ((rgba & 0xFF) << 8) | (rgba & 0xFF);
            else
                stroke = 0xFF000000 | rgba;
        }
        else
            stroke = rgba;
    }
    public DispParent(int rgba, byte newFlags){
        flags = newFlags;
        if((rgba >>> 24) == 0){
            if(rgba <= 0xFF)
                stroke = 0xFF000000 | (rgba << 16) | (rgba << 8) | rgba;
            else if(rgba <= 0xFF00)
                stroke = ((rgba & 0xFF00) << 16) | ((rgba & 0xFF) << 16) | ((rgba & 0xFF) << 8) | (rgba & 0xFF);
            else
                stroke = 0xFF000000 | rgba;
        }
        else
            stroke = rgba;
    }
    
    public void stroke(short r, short g, short b){
        stroke = 0xFF000000 | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }
    public void stroke(short r, short g, short b, short a){
        stroke = ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }
    public void stroke(int rgb, short a){
        if((rgb & 0xFFFFFF) <= 0xFF)
            stroke = ((a & 255) << 24) | (rgb << 16) | (rgb << 8) | rgb;
        else
            stroke = ((a & 255) << 24) | (rgb & 0xFFFFFF);
    }
    public void stroke(int rgba){
        if((rgba >>> 24) == 0){
            if(rgba <= 0xFF)
                stroke = 0xFF000000 | (rgba << 16) | (rgba << 8) | rgba;
            else if(rgba <= 0xFF00)
                stroke = ((rgba & 0xFF00) << 16) | ((rgba & 0xFF) << 16) | ((rgba & 0xFF) << 8) | (rgba & 0xFF);
            else
                stroke = 0xFF000000 | rgba;
        }
        else
            stroke = rgba;
    }

    public void setAlphaStroke(short alpha){
        stroke&=0xFFFFFF;
        alpha&=0xFF;
        stroke = (alpha << 24)|stroke;
    }
    public int returnStroke(){
        return stroke;
    }

    public void setDepthWrite(boolean hasDepthWrite){
        if(hasDepthWrite)
            flags|=1;
        else
            flags&=-2;
    }

    public boolean getHasDepthWrite(){
        return (flags & 1) == 1;
    }

    public void copy(Object o){
        if(o instanceof DispParent){
            DispParent p = (DispParent)o;
            flags = p.flags;
            stroke = p.stroke;
        }
    }
    public void copy(DispParent p){
        flags = p.flags;
        stroke = p.stroke;
    }

    public boolean equals(Object o){
        if(o instanceof DispParent){
            DispParent p = (DispParent)o;
            return flags == p.flags && stroke == p.stroke;
        }
        return false;
    }

    public boolean equals(DispParent p){
        return flags == p.flags && stroke == p.stroke;
    }
}
