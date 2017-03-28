/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bayesian;
import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Dong (Kevin) Zhang
 */
public class csvReader {
    private static final String[] FILTERED = { "Time", "Date" };
    protected int nRow = 0;
    protected String[] title;
    
    public String read(String filename, int initCol){
        String data = "";
        try{
            FileReader f = new FileReader(filename);
            BufferedReader bf = new BufferedReader(f);
            this.title = bf.readLine().split(",");
            
            // Framework of the data: we need read the CSV col by col.
            String[] databody = new String[this.title.length];
            for(int i=0; i< this.title.length; i++){
                if(!this.title[i].contains("I")){
                    databody[i] = this.title[i] + " <- c(";
                }else{
                    databody[i] = "PAR" + " <- c(";
                }
            }
            // Data values
            while (bf.ready()){
                this.nRow += 1;
                String[] row = bf.readLine().split(",");
                for(int i=0; i < row.length; i++){
                    databody[i] += row[i] + ", ";
                }
            }
            // Ending of each column
            for(int i=0; i< this.title.length; i++){
                String tmp = databody[i].substring(0, databody[i].length()-2);
                databody[i] = tmp + ")\n";
            }
            // Create the content of data file
            for(int i=initCol - 1; i< this.title.length; i++){
                data += databody[i];
            }
            // Close the file
            f.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }
   
    /**
     * A parallelized and functional version of Zhang's CSV Reader.
     * @param filename
     * @param initCol
     * @return 
     */
    public String read2(String filename, int initCol){
        String data = "";
        try{
            FileReader f = new FileReader(filename);
            BufferedReader bf = new BufferedReader(f);
            this.nRow = (int) new BufferedReader(new FileReader(filename)).lines().count() - 1;
            this.title = bf.readLine().split(",");
            String[] allLines = Files.lines(Paths.get(filename)).collect(Collectors.toList()).toArray(new String[0]);
            
            // Framework of the data: we need read the CSV col by col.
            String[] databody = new String[this.title.length];
            return Observable.fromArray(allLines[0].split(","))
                    .subscribeOn(Schedulers.computation())
                    // Contains some rule for converting parameters of a certain name to another.
                    .map(t -> t.contains("I") ? "PAR" : t)
                    .map(t -> t + " <- c(")
                    .buffer(Integer.MAX_VALUE)
                    .flatMap(titles -> 
                            Observable.zip(
                                Stream
                                    .of(allLines)
                                    .skip(1)
                                    .map(line -> line.split(","))
                                    .map(Observable::fromArray)
                                    .collect(Collectors.toList())
                            ,arr -> arr)
                            .zipWith(titles, (v, t) -> t + Stream.of(v).map(Object::toString).reduce((str1, str2) -> str1 + ", " + str2).get())
                    )
                    .map(str -> str + ")")
                    .filter(str -> Stream.of(FILTERED).noneMatch(str::startsWith))
                    .reduce("", (line1, line2) -> line1 + line2  + "\n")
                    .blockingGet();
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
    }
    
    public String read(String filename, int initCol, int termCol){
        String data = "";
        try{
            FileReader f = new FileReader(filename);
            BufferedReader bf = new BufferedReader(f);
            this.title = bf.readLine().split(",");
            
            // Framework of the data: we need read the CSV col by col.
            String[] databody = new String[this.title.length];
            for(int i=0; i< this.title.length; i++){
                if(!this.title[i].contains("I")){
                    databody[i] = this.title[i] + " <- c(";
                }else{
                    databody[i] = "PAR" + " <- c(";
                }
            }
            // Data values
            while (bf.ready()){
                String[] row = bf.readLine().split(",");
                for(int i=0; i < row.length; i++){
                    databody[i] += row[i] + ", ";
                }
            }
            // Ending of each column
            for(int i=0; i< this.title.length; i++){
                String tmp = databody[i].substring(0, databody[i].length()-2);
                databody[i] = tmp + ")\n";
            }
            // Create the content of data file
            for(int i=initCol - 1; i< termCol; i++){
                data += databody[i];
            }
            // Close the file
            f.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }
    
}
