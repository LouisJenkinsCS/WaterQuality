/*
 * To read model results into the program for future analysis
 * Files to read are TXTs, fields are seperated using a space
 * Here some idea also:
 * The CSV and TXT here are similar, the only difference is the
 * seperator(CSV using comma). Thus the reader API can be 
 * combined by leaving teh option of seperator to the user.
 */
package bayesian;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dong (Kevin) Zhang
 */
public class resultLoader {
    protected String path;
    protected int nIndexRow = 0;
    
    public resultLoader(String foldername){
        this.path = foldername;
    }
    
    public void JSONconvertIndex(){
        /* Read the given List of String[]
         * The first string of each entry is the name, then values
         * Write the content to the specified file in JSON format
         * This method will convert the outputs to JSON DIRECTLY.
        */
        
        try {
            FileWriter writer = new FileWriter(this.path + "/" + "CODAIndex.json");
            FileReader f = new FileReader(this.path + "/" + "CODAindex.txt");
            BufferedReader bf = new BufferedReader(f);
            writer.write("{\n");
            
            // Read the content row by row
            while (bf.ready()){
                this.nIndexRow += 1;
                String[] row = bf.readLine().split(" ");
                writer.write(row[0] + ":[");

                for(int i=1; i< row.length-1; i++){
                    writer.write(row[i] + ", ");
                }
                writer.write(row[row.length-1] + "]\n");
            }

            writer.write("}");

            // Close the file
            f.close();
            writer.close();
            //System.out.println(indexJSON);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void JSONconvertChain( int chainNum ){
        /* 
         * This method is similar to previous. But, due to
         * the difference between Chain and Index, it will
         * read different fields from the TXT output.
         * This method will convert the outputs to JSON DIRECTLY.
        */
        try {
            FileWriter writer = new FileWriter(this.path + "/" + "CODAchain" + chainNum + ".json");
            FileReader f = new FileReader(this.path + "/" + "CODAchain" + chainNum + ".txt");
            BufferedReader bf = new BufferedReader(f);
            String[] row = bf.readLine().split("  ");
            writer.write("{\n Values:[" + row[row.length - 1]);
            
            // Read the content row by row
            while (bf.ready()){
                row = bf.readLine().split("  ");
                writer.write(", " + row[row.length - 1]);
            }

            writer.write("]\n}");

            // Close the file
            f.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void PrepareDataJS(int Nmeasurements, int nchains, int niter, int nthin, String csvName){
        // Read Index TXT for postion, and mapping to chain TXTs to get value.
        // The method here is designed upon the structure of this bayesian model
      
        try {
            System.out.println(this.path);
            FileReader f_index = new FileReader(this.path + "/" + "CODAindex.txt");
            BufferedReader bf_index = new BufferedReader(f_index);
            FileReader[] f_chains = new FileReader[nchains];
            BufferedReader[] bf_chains = new BufferedReader[nchains];
            for (int j = 0; j < nchains; j++) {
                // Open all chains data for reading
                f_chains[j] = new FileReader(this.path + "/" + "CODAchain" + (j + 1) + ".txt");
                bf_chains[j] = new BufferedReader(f_chains[j]);
            }
            
            int StartPos, EndPos;
            boolean DO_flag = false;    // Used to find the first DO.Model line
            
            int[] currentPos = new int[nchains];
            for(int j=0; j<nchains; j++)
                currentPos[j] = 0;
            
            Map<String, ArrayList<Double>> dataMap = new HashMap<>();
            while (bf_index.ready()) {
                // Get each record line
                String[] row_index = bf_index.readLine().split(" ");
                String name = row_index[0];
                
                FileWriter var = new FileWriter(this.path + "/jsdata/" + csvName + "_" + row_index[0] + ".js");
                String tmp = row_index[0].replace(".", "_");
                if(row_index[0].contains("DO")){
                    if(!DO_flag){
                        var.write("var DO_modelled = new Array();\n");
                        DO_flag = true;
                    }
                    var.write(tmp + " = {\n");
                } else {
                    var.write("var " + tmp + " = {\n");
                }
                
                // L.J: CODAindex.txt contains rows of three variables...
                // NAME START_IDX END_IDX
                StartPos = Integer.parseInt(row_index[1])-1;
                EndPos = Integer.parseInt(row_index[2])-1;
                      
                // Note: All chains they have same dimension. Upon the structure of JSON,
                // we need to read data TXT one-by-one, and each chain provides a List (key by this chain)
                // to the Dictionary named by this variable.
                for(int j=0; j< nchains - 1; j++){
                    dataMap.put(name + "-chain" + (j + 1), new ArrayList<>());
                    var.write("chain" + (j+1) + ":[");
                    // In each CODAchain file, read from START_IDX to END_IDX and read the second row
                    // which contains the actual values for the chart.
                    while (bf_chains[j].ready() && (currentPos[j] >= StartPos && currentPos[j] < EndPos)){
                        String[] row_data = bf_chains[j].readLine().split("  ");
                        dataMap.get(name + "-chain" + (j + 1)).add(Double.parseDouble(row_data[1]));
                        var.write(row_data[1] + ", ");
                        currentPos[j]++;
                    }
                    // Get rid of the last comma inside the List
                    String[] row_data = bf_chains[j].readLine().split("  ");
                    var.write(row_data[1] + "], \n");
                    currentPos[j]++;
                }
                {
                    // One more chain, eliminate the comma after this last List
                    dataMap.put(name + "-chain" + nchains, new ArrayList<>());
                    var.write("chain" + (nchains) + ":[");
                    while (bf_chains[nchains - 1].ready() && (currentPos[nchains - 1] >= StartPos && currentPos[nchains - 1] < EndPos)){
                        String[] row_data = bf_chains[nchains - 1].readLine().split("  ");
                        dataMap.get(name + "-chain" + nchains).add(Double.parseDouble(row_data[1]));
                        var.write(row_data[1] + ", ");
                        currentPos[nchains - 1]++;
                    }
                    String[] row_data = bf_chains[nchains - 1].readLine().split("  ");
                    var.write(row_data[1] + "] \n");
                    currentPos[nchains - 1]++;
                }
                var.write("};");
                var.close();
                dataMap.keySet().stream().forEach(key -> System.out.println("Key: " + key + ", count: " + dataMap.get(key).size()));
                //System.out.println(currentPos[0]);
            }
            f_index.close();
            // Create an extra JS data file to save the indeces of values, i.e. 1 to niter/nthin
            int N = (int)(niter/nthin);
            FileWriter counter = new FileWriter(this.path + "/jsdata/" + csvName + "_" + "counter.js");
            counter.write("var labels = [\n");
            for(int j=1; j<N; j++){
                counter.write(j + ",");
            }
            counter.write(N + "];\n");
            counter.close();
            // Create an extra JS data file to save observation id, i.e. 1 to num.measurement
            FileWriter obs = new FileWriter(this.path + "/jsdata/" + csvName + "_" + "obsID.js");
            obs.write("var obs = [\n");
            for(int j=1; j<Nmeasurements; j++){
                obs.write(j + ",");
            }
            obs.write(Nmeasurements + "];\n");
            obs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void Str2JS(String strValue, String csvName, String varName){
        try {
            System.out.println("Converting : " + strValue);
            String varContent = "var " + varName + " = [";
            FileWriter jsfile = new FileWriter(this.path + "/jsdata/" + csvName + "_" + varName + ".js");  
            varContent += strValue + "];\n";
            jsfile.write(varContent);
            jsfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}