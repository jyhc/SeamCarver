import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    
    private static final double BORDER = 1000;
    private final Picture picture;
    private double[][] energy;
    private final int[][] rgb;
    private int width;
    private int height;
    private double[][] distTo;
    private int[][] fromPixel;
    private boolean vertFlag = false;
    private boolean horizFlag = false;
//    private boolean transposeFlag = false; //to prevent unnecessary tranpose operation
    private int[] tempSeamV;//cache to save operation when dataChangeFlag is false
    private int[] tempSeamH;//cache to save operation when dataChangeFlag is false
    
    public SeamCarver(Picture picture) {// create a seam carver object based on the given picture
        this.picture = new Picture(picture); //create deep copy of picture
        width = picture.width();
        height = picture.height();
        rgb = new int[width][height];
        for(int i = 0; i < width; i++) { //cache rgb values
            for(int j = 0; j < height; j++) {
                rgb[i][j] = picture.getRGB(i, j);
            }
        }
        energy = new double[width][height];
        for(int i = 0; i < width; i++) { //set border energy to 1000
            energy[i][0] = BORDER;
            energy[i][height-1] = BORDER;
        }
        for(int j = 1; j < height - 1; j++) {
            energy[0][j] = BORDER;
            energy[width - 1][j] = BORDER;
        }
        //calculate nontrivial/non-border energies
        for(int i = 1; i < width -1; i++) {
            for(int j = 1; j < height - 1; j++) {
                
                //x axis
                int rgbxl = rgb[i-1][j];
                int rgbxr = rgb[i+1][j];
                //convert from encoding to separate rgb values
                
                int rxl = (rgbxl >> 16) & 0xFF;
                int gxl = (rgbxl >> 8) & 0xFF;
                int bxl = rgbxl & 0xFF;
                
                int rxr = (rgbxr >> 16) & 0xFF;
                int gxr = (rgbxr >> 8) & 0xFF;
                int bxr = rgbxr & 0xFF;
                
                //y axis
                int rgbyu = rgb[i][j-1];
                int rgbyd = rgb[i][j+1];
                
                int ryu = (rgbyu >> 16) & 0xFF;
                int gyu = (rgbyu >> 8) & 0xFF;
                int byu = rgbyu & 0xFF;
                
                int ryd = (rgbyd >> 16) & 0xFF;
                int gyd = (rgbyd >> 8) & 0xFF;
                int byd = rgbyd & 0xFF;

                double energyx = Math.pow((rxl - rxr), 2) + Math.pow((gxl - gxr), 2) + Math.pow((bxl - bxr), 2);
                double energyy = Math.pow((ryu - ryd), 2) + Math.pow((gyu - gyd), 2) + Math.pow((byu - byd), 2);
                
                energy[i][j] = Math.sqrt((energyx + energyy));
            }
        }
        
    }
    
    public Picture picture() {// current picture
        return new Picture(picture);
    }
    
    public int width() {// width of current picture
        return width;
    }
    
    public int height() {// height of current picture
        return height;
    }
    
    public double energy(int x, int y) {// energy of pixel at column x and row y
//        if(transposeFlag == false) return energy[x][y];
//        else return transpose(energy)[x][y];        
        return energy[x][y];
    }
    
//    private double[][] transpose(double[][] data) {
//        double[][] temp = new double[height][width];
//        for (int i = 0; i < width; i++) {
//          for (int j = 0; j < height; j++) {
//            temp[j][i] = data[i][j];
//          }
//        }
//        transposeFlag = !transposeFlag;
//        return temp;
//    }
    
    public int[] findHorizontalSeam() {// sequence of indices for horizontal seam
        if(horizFlag == true) return tempSeamH.clone();//defensive copy
//        if(transposeFlag == false) energy = transpose(energy);
        distTo = new double[width][height];
        fromPixel = new int[width][height];
        for(int i = 0; i < width; i++) {//initialize to infinity then decrease according to topological spt
            for(int j = 0; j < height; j++) {
                distTo[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        for(int h = 0; h < height; h++) {
            updatePathH(0, h, 0 ,h);
        }
        double minDist = distTo[width-1][0];
        int location = 0;
        for(int h = 1; h < height; h++) {
            if(distTo[width-1][h] < minDist) {
                minDist = distTo[width-1][h];
                location = h;
            }
        }
        int[] seam = new int[width];
        seam[width-1] = location;
        for(int w = width-2; w >= 0; w--) {
            seam[w] = fromPixel[w+1][seam[w+1]];
        }
//        int[] copy = new int[seam.length];
//        System.arraycopy(seam, 0, copy, 0, seam.length);
        horizFlag = true;
        tempSeamH = seam.clone();
        return seam.clone();//defensive copy
        
    }
//    
    public int[] findVerticalSeam() {// sequence of indices for vertical seam
        if(vertFlag == true) return tempSeamV.clone();  //defensive copy
//        if(transposeFlag == true) energy = transpose(energy);
        distTo = new double[width][height];
        fromPixel = new int[width][height];
        for(int i = 0; i < width; i++) {//initialize to infinity then decrease according to topological spt
            for(int j = 0; j < height; j++) {
                distTo[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        for(int w = 0; w < width; w++) {
            updatePathV(w, 0, w ,0);
        }
        double minDist = distTo[0][height-1];
        int location = 0;
        for(int w = 1; w < width; w++) {
            if(distTo[w][height-1] < minDist) {
                minDist = distTo[w][height-1];
                location = w;
            }
        }
        int[] seam = new int[height];
        seam[height-1] = location;
        for(int h = height-2; h >= 0; h--) {
            seam[h] = fromPixel[seam[h+1]][h+1];
        }
//        int[] copy = new int[seam.length];
//        System.arraycopy(seam, 0, copy, 0, seam.length);
        vertFlag = true;
        tempSeamV = seam.clone();
        return seam.clone();//defensive copy
    }
    
    private void updatePathV(int sourceX, int sourceY, int currentX, int currentY) {//recursive helper function        
        if(currentX == -1) return;  //past left edge
        if(currentX == width) return; //past right edge
        if(currentY == height) return; //past bottom edge
        
        if(currentY == 0) {
            distTo[currentX][currentY] = energy[currentX][currentY];
            fromPixel[currentX][currentY] = -1;
        }
        else if(distTo[currentX][currentY] > distTo[sourceX][sourceY] + energy[currentX][currentY]) {
            distTo[currentX][currentY] = distTo[sourceX][sourceY] + energy[currentX][currentY];
            fromPixel[currentX][currentY] = sourceX;
        } else return;
        
        updatePathV(currentX,currentY,currentX-1,currentY+1);
        updatePathV(currentX,currentY,currentX,currentY+1);
        updatePathV(currentX,currentY,currentX+1,currentY+1);        
    }
    private void updatePathH(int sourceX, int sourceY, int currentX, int currentY) {//recursive helper function        
        if(currentY == -1) return;  //past top edge
        if(currentX == width) return; //past right edge
        if(currentY == height) return; //past bottom edge
        
        if(currentX == 0) {
            distTo[currentX][currentY] = energy[currentX][currentY];
            fromPixel[currentX][currentY] = -1;
        }
        else if(distTo[currentX][currentY] > distTo[sourceX][sourceY] + energy[currentX][currentY]) {
            distTo[currentX][currentY] = distTo[sourceX][sourceY] + energy[currentX][currentY];
            fromPixel[currentX][currentY] = sourceY;
        } else return;
        
        updatePathH(currentX,currentY,currentX+1,currentY-1);
        updatePathH(currentX,currentY,currentX+1,currentY);
        updatePathH(currentX,currentY,currentX+1,currentY+1);        
    }
//    
//    public void removeHorizontalSeam(int[] seam) {// remove horizontal seam from current picture
//        
//    }
//    
//    public void removeVerticalSeam(int[] seam) {// remove vertical seam from current picture
//        vertCallFlag to false at end
//    }

}