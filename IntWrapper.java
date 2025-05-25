public class IntWrapper{
    public int val = 0;
    public IntWrapper(){
        val = 0;
    }
    public IntWrapper(int newVal){
        val = newVal;
    }
    public String toString(){
        return Integer.toString(val);
    }
}