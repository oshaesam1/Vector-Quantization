public class Block {

    protected int height;
    protected int width;
    protected float[][] numbers;
    protected String code;

   public Block()
    {
        height = 0;
        width = 0;
        code =null;
        numbers = new float[width][height];

    }
    public Block(int height,int width,float [][]numbers,String code) {
        this.height = height;
        this.width = width;
        this.code = code;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.numbers[i][j] = numbers[i][j];
            }
        }

    }



}
