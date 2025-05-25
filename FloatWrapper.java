public class FloatWrapper {
    public float val = 0;
    public FloatWrapper(){
        val = 0;
    }
    public FloatWrapper(float newVal){
        val = newVal;
    }
    public String toString(){
        return Float.toString(val);
    }
}
