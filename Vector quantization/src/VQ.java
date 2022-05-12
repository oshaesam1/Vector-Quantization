import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Vector;
import java.lang.Math.*;

public class VQ {
    public static Block imageBlock = new Block();
    public static Block reconstructed = new Block();
    public static Vector<Block> allBlocks = new Vector<Block>();
    public static Vector<Block> averages = new Vector<>();
    public static Vector<Block> splits = new Vector<>();
    public static Vector<Vector<Block>> nearest = new Vector<Vector<Block>>();
    public static Vector<Block> averages_Last = new Vector();
    public static Vector<Block> decoding = new Vector<Block>();



    //**********read image and set it as a block******
    public static int[][] readImage(String filePath) {
        int width = 0;
        int height = 0;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        int[][] pixels = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;
                pixels[y][x] = r;
            }
        }
        imageBlock.height = height;
        imageBlock.width = width;
        imageBlock.numbers = new float[image.getWidth()][image.getHeight()];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageBlock.numbers[i][j] = Float.valueOf(pixels[i][j]);
            }
        }

        return pixels;
    }


    //*********************Calculate Average block**************
    public static Block avgBlock(Vector<Block> Blocks){
        Block avg = new Block();
        int width = allBlocks.get(0).width;
        int height = allBlocks.get(0).height;
        int counter=-1;
        avg.width = width;
        avg.height = height;
        avg.numbers = new float[width][height];
        while (++counter!= Blocks.size()) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    avg.numbers[i][j] += Blocks.get(counter).numbers[i][j];
                }
            }
        }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    avg.numbers[i][j] /= Blocks.size();

                }

    }
        return avg;}
   //**********************Split Averages function******
    public static Vector<Block> splitAverage(Vector<Block> averages) {
        for (int i = 0; i < averages.size(); i++) { //divide every avg block into 2 blocks
            Block first = new Block();
            Block second = new Block();

            first.width = averages.get(i).width;
            first.height = averages.get(i).height;
            first.numbers = new float[averages.get(i).width][averages.get(i).height];
            second.width = averages.get(i).width;
            second.height = averages.get(i).height;
            second.numbers = new float[averages.get(i).width][averages.get(i).height];

            for (int x = 0; x < first.width; x++) {
                for (int y = 0; y < first.height; y++) {

                    first.numbers[x][y] = (float) Math.floor(averages.get(i).numbers[x][y]);
                    second.numbers[x][y] = (float) Math.ceil(averages.get(i).numbers[x][y]);

                }
            }
            splits.add(first);
            splits.add(second);
        }
        return splits;
    }

    //**********************get the Nearest ******

 public static Vector<Vector<Block>> nearest( Vector<Block> Blocks, Vector<Block> splits){
nearest.clear();;
     Block block=new Block();
     for (int i = 0; i < splits.size(); i++) {
         Vector<Block> temp = new Vector<>();
         nearest.add(temp);
     }

     for(int i=0;i<Blocks.size();i++){
         block=calculate(Blocks.get(i),splits);
        int indx= search(block,splits);
         nearest.get(indx).add(Blocks.get(i));
     }
     return nearest;
 }

    public static Block calculate(Block block, Vector<Block>splits){

        int indx=0;
        int mini=10000000;
        for(int i=0;i<splits.size();i++){
            int distance=0;
            for(int l=0;l<block.width;l++){
                for(int j=0;j<block.height;j++){
                    distance+= Math.pow(block.numbers[l][j]-splits.get(i).numbers[l][j],2);
                }
            }
            if(distance<mini){
                mini=distance;
                indx=i;}
        }

        return splits.get(indx);

    }
    public static int search(Block block,Vector<Block> vector)
    {
        int index =-1;
        for (int i = 0;i < vector.size();i++)
            for (int j = 1;j < vector.size();j++)
                if(vector.get(i).numbers == block.numbers)
                    index= i;
        return index;
    }

    //**********************Assign codes ******
    public static void assignCode(int codebookSize,Vector<Block> averages){

        int bits = (int)(Math.log(codebookSize - 1) /  Math.log(2) + 1); //calculate num of bits

        for(int i = 0; i < codebookSize; i++)
        {
            String code  = Integer.toBinaryString(i);

            String store = "";
            if(code.length()<bits)
            {
                for(int j = 0; j < bits-code.length(); j++)
                    store+="0";
            }
            code = store + Integer.toBinaryString(i);

            averages.get(i).code=code;

        }

    }
//************************reconstruct and write image*********************

    public static void writeImage(int[][] imagePixels, int width, int height, String outPath) {
        BufferedImage image = getBufferedImage(imagePixels, width, height);
        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static BufferedImage getBufferedImage(int[][] imagePixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            int s;
            if(y == 199)
                s = 13;
            for (int x = 0; x < width; x++) {
                int value = -1 << 24;
                value = 0xff000000 | (imagePixels[y][x] << 16) | (imagePixels[y][x] << 8) | (imagePixels[y][x]);
                image.setRGB(x, y, value);
            }
        }
        return image;
    }


    public static boolean Compare_Final_Blocks(Vector<Block> one, Vector<Block> two) {

            for(int i = 0; i < one.size(); ++i) {
                for(int w = 0; w < imageBlock.width; ++w) {
                    for(int g = 0; g < imageBlock.height; ++g) {
                        if (((Block)one.get(i)).numbers[w][g] != ((Block)two.get(i)).numbers[w][g]) {
                            return false;
                        }
                    }
                }
            }


        return true;
    }

public static int[][] reconstructPixels()
    {

        int[][]ImagePixels=new int[imageBlock.width][imageBlock.height];
        int myheight=0 ,firstHeight=0;
        int mywidth=0,firstWidth=0;
        for(int x = 0 ; x < allBlocks.size() ; x++ ){

            for(int  i=0 ; i < allBlocks.get(1).width ; i++){
                for(int  j=0 ; j< allBlocks.get(1).height ; j++){
                    ImagePixels[mywidth][myheight++]=(int)allBlocks.get(x).numbers[i][j];
                    if (j+1==allBlocks.get(1).height&&i+1!=allBlocks.get(1).width)
                    {
                        myheight = firstHeight;
                    }

                }
                mywidth++;
                }
            if(myheight!= imageBlock.height){mywidth=firstWidth;
           firstHeight= myheight;
            }


            else
            {
                firstWidth=mywidth;
                myheight=0;
                firstHeight=0;
            }
            }

        return ImagePixels;
    }

    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);
        boolean flag = true;
        int e = 0;
        System.out.println("Enter image path ");
        String path = keyboard.nextLine();
        System.out.println("Enter vector width and height ");
        int width = keyboard.nextInt();
        int height = keyboard.nextInt();
        System.out.println("Enter code book size ");
        int codeBookSize = keyboard.nextInt();
        readImage(path);


        //*******************************************Compressing****************************//

        // ****dividing into blocks****
        for (int i = 0; i < imageBlock.width; i += width) {
            for (int j = 0; j < imageBlock.height; j += height) {
                Block b = new Block();
                b.height = height;
                b.width = width;
                b.numbers = new float[width][height];
                //inside block
                int x = 0, y = 0;
                for (int m = i; m < i + width; m++, x++) {
                    for (int n = j; n < j + height; n++, y++) {
                        b.numbers[x][y] = imageBlock.numbers[m][n];
                    }
                    y = 0;
                }
                x = 0;
                y = 0;
                allBlocks.add(b);
            }
        }

        //************Apply Splitting***************

        Block avg = new Block();
        avg.width = width;
        avg.height = height;
        avg.numbers = new float[width][height];
while (true){
            int i;
            if (flag) {
                avg = avgBlock(allBlocks);
                averages.add(avg);
                splits = splitAverage(averages);
                nearest = nearest(allBlocks, splits);
                flag = false;
            } else {
                averages.clear();

                for(i = 0; i < nearest.size(); ++i) {
                    avg = avgBlock((Vector)nearest.get(i));
                    averages.add(avg);
                }

                if (averages.size() < codeBookSize) {
                    splits.clear();
                    splits = splitAverage(averages);
                }

                if (averages.size() < codeBookSize) {
                    nearest = nearest(allBlocks, splits);
                } else {
                    nearest = nearest(allBlocks, averages);
                }
            }


            if (averages.size() == codeBookSize) {
                if (e == 0) {
                    averages_Last.addAll(averages);
                    ++e;
                } else {
                    if (Compare_Final_Blocks(averages, averages_Last)) {
                        int j;
                        for (i = 0; i < 2; ++i) {
                            nearest = nearest(allBlocks, averages);
                            averages.clear();

                            for (j = 0; j < nearest.size(); ++j) {
                                avg = avgBlock((Vector) nearest.get(j));
                                averages.add(avg);
                            }
                        }

                        int x;
                        int y;
                        Block block;
                        for (i = 0; i < codeBookSize; ++i) {
                            new Block();
                            block = (Block) averages.get(i);

                            for (x = 0; x < width; ++x) {
                                for (y = 0; y < height; ++y) {
                                    block.numbers[x][y] = (float) ((int) block.numbers[x][y]);
                                }
                            }
                        }
                    }
                        assignCode(codeBookSize, averages);

                        for(i = 0; i < splits.size(); ++i) {
                            for(int j = 0; j < ((Vector)nearest.get(i)).size(); ++j) {
                                ((Block)((Vector)nearest.get(i)).get(j)).code = ((Block)averages.get(i)).code;
                            }
                        }

                        System.out.println("*******code book***************");

                        for(i = 0; i < codeBookSize; ++i) {

                            System.out.println("code : " + averages.get(i).code);

                            for(int x = 0; x < width; ++x) {
                                for(int y = 0; y < height; ++y) {
                                    System.out.print((int)averages.get(i).numbers[x][y] + "  ");
                                }

                                System.out.println();
                            }
                        }



                    System.out.println("*******compressed image***************");

                    for(i = 0; i < allBlocks.size(); ++i) {

                                System.out.print((allBlocks.get(i).code)+ " ");


                    }


                    for(i = 0; i < allBlocks.size(); ++i) {
                            int k = 0;
                            if (k < codeBookSize && ((Block)allBlocks.get(i)).code.equals((averages.get(k).code))) {
                                for(int x = 0; x < width; ++x) {
                                    for(int y = 0; y < height; ++y) {
                                        (allBlocks.get(i)).numbers[x][y] = (float) ((int)(averages.get(k)).numbers[x][y]);
                                    }
                                }
                            }
                        }

                        int[][] pixels = reconstructPixels();
                        writeImage(pixels, imageBlock.width, imageBlock.height, "C:\\Users\\hp\\IdeaProjects\\Vector quantization\\new.jpg");
                        return;
                    }

                    averages_Last.addAll(averages);
                }
            }

}}

