package searchexperimentnetbeans;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
/**
 * Maeda Hanfi Search Experiment 3/8/10
 */

public class experiment extends Frame implements ActionListener{
    final int ARRAY_SIZE = 500000;
    final int BUCKET_SIZE = 50;
    // File Parameters
    String dataFilePath = null;
    String dataFileName = null;
    String keyFilePath = null;
    String keyFileName = null;

    // Retrieved command code
    String command = "";

    //array
    int[] originalArray = new int[ARRAY_SIZE];
    int[] sortedArray = new int[ARRAY_SIZE];
    int[] keysArray = new int[ARRAY_SIZE];
    int[] hashedArray = new int[2*ARRAY_SIZE];
    int[][] bucketHashedArray ;

    //number of data items
    int numberOfDataItems = 0;
    int numberOfKeys = 0;
    int M =0;
    int M1 ;
   
    //average access time
    long sAverageAccessTime, bAverageAccessTime, hAverageAccessTime, bHAverageAccessTime;
    //aver comparisons
    double SAverageSuccessfulComp, SAverageUnsuccessfulComp;
    double BAverageSuccessfulComp, BAverageUnsuccessfulComp;
    double HAverageSuccessfulComp, HAverageUnsuccessfulComp;
    double bHAverageSuccessfulComp, bHAverageUnsuccessfulComp;
    //found and unfound keys
    double SFoundKeys, SNotFoundKeys;
    double BFoundKeys, BNotFoundKeys;
    double HFoundKeys, HNotFoundKeys;
    double bHFoundKeys, bHNotFoundKeys;

    int hTotalComparisons;
    int bHTotalComparisons;
    JTextArea textArea;
    String result = "";

    public static void main(String[] args){
        Frame frame = new experiment();

        frame.setResizable(false);
        frame.setSize(900,500);
        frame.setVisible(true);
    }

    public experiment(){
        setTitle("Search Routines");

        // Create Menu Bar
        MenuBar mb = new MenuBar();
        setMenuBar(mb);

        // Create Menu Group Labeled "File"
        Menu menu = new Menu("File");
        mb.add(menu);

        MenuItem miOpen = new MenuItem("Open");
        miOpen.addActionListener(this);
        menu.add(miOpen);

        MenuItem miExit = new MenuItem("Exit");
        miExit.addActionListener(this);
        menu.add(miExit);

        Menu menuSearch = new Menu("Search");
        mb.add(menuSearch);

        MenuItem miSearch = new MenuItem("Search");
        miSearch.addActionListener(this);
        menuSearch.add(miSearch);

        // End program when window is closed
        WindowListener l = new WindowAdapter(){
                public void windowClosing(WindowEvent ev){
                        System.exit(0);
                }
                public void windowActivated(WindowEvent ev){
                        repaint();
                }
                public void windowStateChanged(WindowEvent ev){
                        repaint();
                }
        };

        ComponentListener k = new ComponentAdapter(){
                public void componentResized(ComponentEvent e)
                {
                repaint();
        }
        };

        //to display result
        textArea = new JTextArea();
        textArea.setFont(new Font("Serif", Font.PLAIN, 16));
        textArea.setEditable(false);
        this.add(textArea);
        this.pack();
        this.setVisible(true);

        this.addWindowListener(l);
        this.addComponentListener(k);
    }

    public void actionPerformed (ActionEvent ev){
            // figure out which command was issued
            command = ev.getActionCommand();

            // take action accordingly
            if("Open".equals(command)){
                initialize();
                JFileChooser dataChooser = new JFileChooser();
                dataChooser.setDialogType(JFileChooser.OPEN_DIALOG );
                dataChooser.setDialogTitle("Open Data File");

                if( dataChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                      dataFilePath = dataChooser.getSelectedFile().getPath();
                      dataFileName = dataChooser.getSelectedFile().getName();
                }
                try{
                    numberOfDataItems = readFileToArray(dataFilePath, "Original");
                    M1 = getBucketHashedOperand();
                    bucketHashedArray = new int[BUCKET_SIZE][numberOfDataItems];
                    for(int i=0; i<BUCKET_SIZE; i++){
                        for(int j=0; j<numberOfDataItems; j++){
                            bucketHashedArray[i][j] = -1;
                        }
                    }
                }catch(IOException ex){
                    System.exit(0);
                }

                JFileChooser keyChooser = new JFileChooser();
                keyChooser.setDialogType(JFileChooser.OPEN_DIALOG );
                keyChooser.setDialogTitle("Open Data File");

                if( keyChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                      keyFilePath = keyChooser.getSelectedFile().getPath();
                      keyFileName = keyChooser.getSelectedFile().getName();
                }
                try{
                    numberOfKeys = readFileToArray(keyFilePath, "Keys");
                }catch(IOException ex){ 
                    System.exit(0);
                }
               
                repaint();
            }else if("Search".equals(command)){
                headerReport();
                sequentialSearch();
                sort();
                binarySearch();
                hashedSearch();
                bucketHashedSearch();
                repaint();
            }else if("Exit".equals(command)){
                System.exit(0);
            }
    }

    public void paint(Graphics g){
        if("Open".equals(command)){
            // Acknowledge that file was opened
            if (dataFileName != null){
                    g.drawString("File of data--  "+dataFileName+"  -- was successfully opened", 200, 200);
            }else{
                    g.drawString("NO File is Open", 200, 200);
            }
            if (keyFileName != null){
                    g.drawString("File of keys--  "+keyFileName+"  -- was successfully opened", 200, 400);
            }else{
                    g.drawString("NO File is Open", 200, 400);
            }
            
            return;
        }else if("Search".equals(command)){
            //display
            textArea.setText(result);
            return;
        }
    }
    
    public void headerReport(){
        result = "";
        result = result + "\t\t\t\tNumber Of Data Items: " + numberOfDataItems + ", Number Of Keys: " + numberOfKeys;
        result = result + "\n\t\tAverage\tSuccessful Search\t\tUnsuccessful Search";
        result = result + "\n\t\tAccess\t# Keys\tAverage #\t\t# Keys not\tAverage #";
        result = result + "\n\t\tTime\tFound\tof Comparisons\tFound\tof Comparisons";
    }

    public void initialize(){
        result = "";
        initFiles();
        initAverTime();
        initKeys();
        initComparisons();
        initArray();
    }

    public void initFiles(){
        dataFilePath = null;
        dataFileName = null;
        keyFilePath = null;
        keyFileName = null;
    }

    public void initAverTime(){
         sAverageAccessTime = 0;
        bAverageAccessTime = 0;
        hAverageAccessTime = 0;
    }

    public void initKeys(){
        SFoundKeys = 0;
        SNotFoundKeys = 0;
        BFoundKeys = 0;
        BNotFoundKeys = 0;
        HFoundKeys = 0;
        HNotFoundKeys = 0;
    }

    public void initComparisons(){
        SAverageSuccessfulComp = 0;
        SAverageUnsuccessfulComp = 0;
        BAverageSuccessfulComp = 0;
        BAverageUnsuccessfulComp = 0;
        HAverageSuccessfulComp = 0;
        HAverageUnsuccessfulComp = 0;

        hTotalComparisons = 0;
        bHTotalComparisons = 0;
    }

    public void initArray(){
        for(int i=0; i<ARRAY_SIZE; i++){
            sortedArray[i] = 0;
            originalArray[i] = 0;
            keysArray[i] = 0;
        }
        for(int i=0; i<(2*ARRAY_SIZE); i++){
            hashedArray[i] = -1;
        }
    }

    public int readFileToArray(String filePath, String type) throws IOException{
        if(filePath != null){
            int index = 0;
            Scanner integerTextFile = new Scanner(new File(filePath));
            while(integerTextFile.hasNext()){
                if("Original".equals(type)){
                    originalArray[index] = integerTextFile.nextInt();
                }else{
                    keysArray[index] = integerTextFile.nextInt();
                }
                index++;
            }
            integerTextFile.close();
            return index;
         }else{
            return 0;
         }
    }

    public void sequentialSearch(){
        double successfulComp = 0.0, unsuccessfulComp = 0.0;
        int totalComparisons = 0;
        result = result + "\nSequential Search:";
        long startTime = System.nanoTime();
        //search
        //take a key from key Array
        for(int k=0; k<numberOfKeys; k++){
            boolean found = false;
            totalComparisons = 0;
            //find it in the data array(original array)
            for(int d=0; d<numberOfDataItems; d++){
                totalComparisons++;
                if(keysArray[k]==originalArray[d]){
                    //succesfully found key
                    found = true;
                    break;
                }
            }
            if(found){
                successfulComp = successfulComp + totalComparisons;
                SFoundKeys++;

            }else{
                unsuccessfulComp = unsuccessfulComp + totalComparisons;
                SNotFoundKeys++;
            }
        }
        long estimatedTime = System.nanoTime() - startTime;
        if(numberOfKeys != 0){
            sAverageAccessTime = Math.round((estimatedTime/numberOfKeys));
        }else{
            sAverageAccessTime = 0;
        }
        if(SFoundKeys != 0){
            SAverageSuccessfulComp = successfulComp/SFoundKeys;
        }else{
            SAverageSuccessfulComp = 0;
        }
        if(SNotFoundKeys != 0){
            SAverageUnsuccessfulComp = unsuccessfulComp/SNotFoundKeys;
        }else{
            SAverageUnsuccessfulComp = 0;
        }
        
        result = result + "\t" + sAverageAccessTime + "\t" +SFoundKeys+"\t"
                + formatNumber(SAverageSuccessfulComp)+ "\t\t" +SNotFoundKeys+ "\t" + formatNumber(SAverageUnsuccessfulComp);
       
        return;
    }
    public String formatNumber(double inDouble){
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        DecimalFormat decimalFormat = (DecimalFormat)numberFormat;
        decimalFormat.applyPattern("0.0##");
        return decimalFormat.format(inDouble);
    }
    public void sort(){
        System.arraycopy(originalArray, 0, sortedArray, 0, numberOfDataItems);
        Arrays.sort(sortedArray, 0, numberOfDataItems);
    }
    
    public void binarySearch(){
        double successfulComp = 0.0, unsuccessfulComp = 0.0;
        int totalComparisons = 0;
        result = result + "\nBinary Search";
        long startTime = System.nanoTime();

        //go thru each key
        for(int k=0; k<numberOfKeys; k++){
            //search tru the data array
            totalComparisons = 0;
            boolean found = false;
            int low = 0,  high = numberOfDataItems;
            while(high>=low){
                totalComparisons++;
                int mid = (low + high) / 2;
                if (keysArray[k] < sortedArray[mid]){//key is less than the content of midpoint
                    high = mid - 1;
                }else if (keysArray[k] ==  sortedArray[mid]){//found it!
                    found = true;
                    break;
                }else{//key is greaeter than the content of midpoint
                    low = mid + 1;
                    
                }
            }
            if(found){
                successfulComp = successfulComp + totalComparisons;
                BFoundKeys++;
            }else{
                unsuccessfulComp = unsuccessfulComp + totalComparisons;
                BNotFoundKeys++;
            }
        }

        long estimatedTime = System.nanoTime() - startTime;
        if(numberOfKeys != 0){
            bAverageAccessTime = Math.round((estimatedTime/numberOfKeys));
        }else{
            bAverageAccessTime = 0;
        }
        if(BFoundKeys != 0){
            BAverageSuccessfulComp = successfulComp/BFoundKeys;
        }else{
            BAverageSuccessfulComp = 0;
        }
        if(BNotFoundKeys != 0){
            BAverageUnsuccessfulComp = unsuccessfulComp/BNotFoundKeys;
        }else{
            BAverageUnsuccessfulComp = 0;
        }
        result = result + "\t\t" + bAverageAccessTime + "\t" +BFoundKeys+"\t"
                + formatNumber(BAverageSuccessfulComp)+ "\t\t" +BNotFoundKeys+ "\t" + formatNumber(BAverageUnsuccessfulComp);
        return;
    }

    public void hashedSearch(){
        result = result + "\nHashed  Search";
        buildHashed();
        searchHashedArray();
    }
    
    public void buildHashed(){
        int index, temp = 0;
        M = getHashedOperand();
      
        //hash original data array
        for(int i=0; i<numberOfDataItems; i++){
            index = originalArray[i]%M;
            if(hashedArray[index]==-1){
                hashedArray[index] = originalArray[i];
            }else{
                temp = index;
                temp++;
                temp = linearProbe(temp);
                hashedArray[temp] = originalArray[i];
            }
        }
    }

    public int linearProbe(int index){
        for(int i=index; i<2*numberOfDataItems; i++){
            if(hashedArray[i]==-1){
                return i;
            }
        }
        //reset to search up to index
        for(int i=0; i<index; i++){
            if(hashedArray[i]==-1){
                return i;
            }
        }
        return 0;
    }


    public int getHashedOperand(){
        int N = 0;
        int num = 2  * numberOfDataItems;
        //find largest prime
        for(int j=num; j>=0; j--){
            int i;
            for (i=2; i < j ;i++ ){
                if (j%i==0){//not prime
                    break;
                }
            }
            if(i == j){//prime
               N=i;
               break;
            }
        }
        return N;
    }

    public void searchHashedArray(){
        double successfulComp = 0.0, unsuccessfulComp = 0.0;
        long startTime = System.nanoTime();
        for(int k=0; k<numberOfKeys; k++){
            hTotalComparisons = 0;
            boolean found = false;
            hTotalComparisons++;
            //go thru hashed array
            int index = keysArray[k]%M;
            if(keysArray[k]==hashedArray[index]){
                found = true;
            }else{
                int temp = index+1;
                temp = probeHashedArray(index, keysArray[k]);
                if(temp==-1){
                    //System.out.println(keysArray[k]+" is not found");
                }else{
                    //System.out.println("found "+keysArray[k]+" at "+temp+" by probing");
                    found = true;
                }
             }
            if(found){
                successfulComp = successfulComp + hTotalComparisons;
                HFoundKeys++;
            }else{
                unsuccessfulComp = unsuccessfulComp + hTotalComparisons;
                HNotFoundKeys++;
            }
        }
        long estimatedTime = System.nanoTime() - startTime;
        if(numberOfKeys != 0){
            hAverageAccessTime = Math.round((estimatedTime/numberOfKeys));
        }else{
            hAverageAccessTime = 0;
        }
        if(HFoundKeys != 0){
            HAverageSuccessfulComp = successfulComp/HFoundKeys;
        }else{
            HAverageSuccessfulComp = 0;
        }
        if(BNotFoundKeys != 0){
            HAverageUnsuccessfulComp = unsuccessfulComp/HNotFoundKeys;
        }else{
            HAverageUnsuccessfulComp = 0;
        }
        result = result + "\t" + hAverageAccessTime + "\t" +HFoundKeys+"\t"
                + formatNumber(HAverageSuccessfulComp)+ "\t\t" +HNotFoundKeys+ "\t" + formatNumber(HAverageUnsuccessfulComp);

        System.out.println("Average Access time: " + hAverageAccessTime);
        System.out.println("Number of Found Keys: " + HFoundKeys);
        System.out.println("Number of Unfound Keys: " + HNotFoundKeys);
        System.out.println("Average Number of Successful Comparisons: " + HAverageSuccessfulComp);
        System.out.println("Average Number of Unsuccessful Comparisons: " + HAverageUnsuccessfulComp);
    }

    public int probeHashedArray(int index, int key){
        for(int i=index; i<numberOfKeys; i++){
            hTotalComparisons++;
            if(key==hashedArray[i]){
                return i;
            }
        }
        for(int i=0; i<index; i++){
            hTotalComparisons++;
            if(key==hashedArray[i]){
                return i;
            }
        }
        return -1;
    }
    public void bucketHashedSearch(){
        double successfulComp = 0.0, unsuccessfulComp = 0.0;
        result = result + "\nBucket Hashed  Search";
        
        boolean found;
        buildHashForBucket();
        long startTime = System.nanoTime();
        for(int i=0; i<numberOfKeys; i++){
           int row = keysArray[i] % BUCKET_SIZE;
           int col = keysArray[i] % M1;
            found = false;
            //bucketTotalComparisons = 0;
            bHTotalComparisons=0;
            bHTotalComparisons++;
           if(bucketHashedArray[row][col]==keysArray[i]){
               //System.out.println(keysArray[i]+" is found");
               found = true;

           }else{
                //probe
                int temp = col + 1;
                temp = probeBucketHashedArray(row, temp, keysArray[i]);
                if(temp==-1){
                    //System.out.println(keysArray[i]+" is not found");
                }else{
                    //System.out.println("found "+keysArray[i]+" at bucklet "+row + ", " + temp+" by probing");
                    found = true;
                }
                
           }
            
            if(found){
                successfulComp += bHTotalComparisons;
                bHFoundKeys++;
            }else{
                unsuccessfulComp+= bHTotalComparisons;
                bHNotFoundKeys++;
           }
        }
        long estimatedTime = System.nanoTime() - startTime;

        bHAverageAccessTime = (long) averageAccessTime(estimatedTime);
        bHAverageSuccessfulComp = computeSearchAverage(successfulComp, bHFoundKeys);
        bHAverageUnsuccessfulComp = computeSearchAverage(unsuccessfulComp, bHNotFoundKeys);
        /*if(HFoundKeys != 0){
            HAverageSuccessfulComp = successfulComp/HFoundKeys;
        }else{
            HAverageSuccessfulComp = 0;
        }
        if(BNotFoundKeys != 0){
            HAverageUnsuccessfulComp = unsuccessfulComp/HNotFoundKeys;
        }else{
            HAverageUnsuccessfulComp = 0;
        }*/
        result = result + "\t" + bHAverageAccessTime + "\t" +bHFoundKeys+"\t"
                + formatNumber(bHAverageSuccessfulComp)+ "\t\t" +bHNotFoundKeys+ "\t" + formatNumber(bHAverageUnsuccessfulComp);

        System.out.println("Average Access time: " + bHAverageAccessTime);
        System.out.println("Number of Found Keys: " + bHFoundKeys);
        System.out.println("Number of Unfound Keys: " + bHNotFoundKeys);
        System.out.println("Average Number of Successful Comparisons: " + bHAverageSuccessfulComp);
        System.out.println("Average Number of Unsuccessful Comparisons: " + bHAverageUnsuccessfulComp);

    }

    public int probeBucketHashedArray(int inRow, int inCol, int inKey) {
        for (int col=inCol; col < numberOfDataItems; col++) {
            //bucketTotalComparisons++;
            bHTotalComparisons++;
            if(bucketHashedArray[inRow][col]==inKey){
                return col;
            }
        }

        for (int col=0; col < inCol; col++) {
            bHTotalComparisons++;
            if(bucketHashedArray[inRow][col]==inKey){
                return col;
            }
        }

        return -1;
    }

    public double averageAccessTime(long estimatedTime){
        if(numberOfKeys != 0){
            return Math.round((estimatedTime/numberOfKeys));
        }else{
            return 0;
        }
    }

    public double computeSearchAverage(double inResult, double inKeys){
        if(inKeys != 0){
            return inResult/inKeys;
        }else{
            return 0;
        }
    }

    public void buildHashForBucket(){
        
        System.out.println("M1 is "+M1);
        int temp = 0;
        int row, col;
        for(int i=0; i<numberOfDataItems; i++){
            row = originalArray[i] % BUCKET_SIZE;
            col = originalArray[i] % M1;

            if(bucketHashedArray[row][col]==-1){
                bucketHashedArray[row][col] = originalArray[i];
            }else{
                temp = col;
                temp++;
                temp = linearProbeInABucket(row, temp);
                bucketHashedArray[row][temp] = originalArray[i];
            }
        }

    }
    public int getBucketHashedOperand(){
        int N = 0;
        int num = numberOfDataItems;//2  * numberOfDataItems;//BUCKET_SIZE;
        //find largest prime
        for(int j=num; j>=0; j--){
            int i;
            for (i=2; i < j ;i++ ){
                if (j%i==0){//not prime
                    break;
                }
            }
            if(i == j){//prime
               N=i;
               break;
            }
        }
        return N;
    }

    public int linearProbeInABucket(int inBucket, int inCol){
        for(int i=inCol; i<numberOfDataItems; i++){
            if(bucketHashedArray[inBucket][inCol]==-1){
                return inCol;
            }
        }
        //reset to search up to index
        for(int i=0; i<inCol; i++){
            if(bucketHashedArray[inBucket][inCol]==-1){
                return inCol;
            }
        }
        return 0;
    }
}




