/*
 * Fitting the Bayesian model using JAGS
 * In the package, APIs of JAGS have been defined
 * 
 * Since Javascript will be used to draw the plots
 * instead of using Java graphics, the program will
 * generate the JS file to draw the plots.
 */
package bayesian;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Dong Zhang
 */
public class RunBayesianModel {
    /* 
    * Some arguments will be passed to here using args: args[1] is model text file. 
    * Or you can use a confid file but involving File IO Stream here.
    */
    private static String model_name = "BASE_metab_model_v2.1.txt";
    private static String user_dir = RunBayesianModel.class.getResource(".").getPath().substring(1);
    private static String base_dir = user_dir + "BASE\\";
    private static String input_dir = base_dir + "input\\"; 
    private static String output_dir = base_dir + "output\\";
    private static String jsroot_dir = base_dir + "JSchart\\";
    private static int interval = 600; // IMO, this variable is used to generate instant_rate table.
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        
        FileOperation fo = new FileOperation();
        // For each CSV file
        for (File f : fo.fileFilter(input_dir, "csv")) {
            ForkJoinPool.commonPool().execute(() -> runJJAGSForCSV(fo, f));
        }
        
        ForkJoinPool.commonPool().awaitQuiescence(Integer.MAX_VALUE, TimeUnit.DAYS);
    }
    
    private static void runJJAGSForCSV(FileOperation fo, File f) {
        System.out.println("Processing " + f.getName());

        //Fitting the model and obtain the results in the folder output/
        String tmp_dir = base_dir + f.getName() + "/";

        // Create the temporary folder for current CSV calculation
        fo.newFolder(tmp_dir);
        // Create the output folder for current CSV calculation inside output/
        fo.newFolder(output_dir + f.getName());

        // Prepare the data
        csvReader content = new csvReader();
        String cnt = content.read(input_dir + f.getName(), 3);
        System.out.println("Row: " + content.nRow);
        // String cnt = content.read(input_dir + f.getName(), 3, 4);
        String datafile = "num.measurements <- c(" + content.nRow + ")\n"
                + "interval <- c(" + interval + ")\n"
                + cnt;
        fo.newFile(tmp_dir + "data.txt", datafile);

        // JAGS object
        // If you have path to jags in your PATH, you can use "jags" only without path to it.
        // ---- Windows JAGS ----
         JJAGS jg = new JJAGS("\"C:/Program Files/JAGS/JAGS-4.2.0/x64/bin/jags.bat\"", tmp_dir, f.getName(), base_dir + model_name);
        // ---- Linux JAGS ----
//            JJAGS jg = new JJAGS("/usr/bin/jags", tmp_dir, base_dir + model_name);            

        // Variables to be monitored. They will be used to create graphs. You can read a config file to get them.
        String[] monitor = {"A","R","K","K.day","p","theta","sd","ER","GPP", "NEP","sum.obs.resid","sum.ppa.resid","PPfit","DO.modelled"};  
        String[] init = {""};   // Initial values to the model, can be blank.
        int nchains = 3, nthin = 10, niter = 20000, nburnin = (int) (niter * 0.5);
        jg.make_script(nchains, nthin, monitor, nburnin, niter);
        jg.make_init(123, init); // Random seed can be changed for different CSV files
        jg.jags_run();  // Here if the app running properly, you will get "Job done" in the terminal

        // Handle the outputs of JAGS: By default they are inside the project folder...
        for(File g : fo.fileFilter(user_dir, "txt")){
            System.out.println("Would have moved: " + user_dir + g.getName() + " to " + output_dir + f.getName() + "/" + g.getName());
//            fo.moveFile(user_dir + g.getName(), output_dir + f.getName() + "/" + g.getName());
        }
        

        // Clean the cache of calculation
        fo.delFolder(tmp_dir);

        /*
        Here, the output TXT files will be converted to JSON format.
        Result files in output_dir/f.getName()/ are:
        1. CODAIndex.txt --  Telling the row index of core results
        2. CODAchain?.txt -- A sequence of core results for each chain
        * Well, it seems convert TXT to JSON is useless...  (=,=)#
        * Since JS is limited to access local files, the better way is
        * write all values to JS files directly and loaded in html, which
        * is written in the next section.
        */
        //resultLoader rl = new resultLoader(output_dir + f.getName());
        //rl.JSONconvertIndex();
        //for(int i=1; i<= nchains; i++){
        //    rl.JSONconvertChain(i);
        //}

        /*
         * It will be the Javascript JS file's job to create charts.
         * Methods to recognize all needed variables must be written,
         * and covert it to JSON format and save to JS files.
         * The Template.html will be used to create html files for each CSV
         * Note: The template is NOT a completed html file, therefore
         * JS codes can be appended to them. The porgram will complete them
         * after pushing codes, thus they can be run directly in explorer.
        */
        // Create html file and JS files for the CSV file using the template
        fo.copyFile(jsroot_dir + "Chart.bundle.js", output_dir + f.getName() + "/" + "Chart.bundle.js");
        fo.copyFile(jsroot_dir + "jquery-3.1.1.min.js", output_dir + f.getName() + "/" + "jquery-3.1.1.min.js");
        fo.newFolder(output_dir + f.getName() + "/jsdata");

        resultLoader rl = new resultLoader(output_dir + f.getName());
        // Parsing all variables out from TXT outputs and create JS files for each
        rl.PrepareDataJS(content.nRow, nchains, niter, nthin, f.getName());
        // Generate the graph JS file
        String plotVarList = "A,R,p,K.day,theta"; // Do NOT place "spaces"!!!
        String scatterList = "PAR,tempC,DO.modelled";   // SAME as above!!!
        JSgen csvjs = new JSgen(output_dir + f.getName());
        csvjs.CodingHTML(f.getName(), (plotVarList + "," + scatterList).split(","), content.nRow);
        csvjs.codingJS(nchains, plotVarList.split(","), scatterList.split(","));
        csvjs.codingStatJS(content.nRow, (int)(niter/nthin), nchains);

        // Scatter plots of PAR and tempC
        int offset = 6;
        String tmp = content.read(input_dir + f.getName(), 3, 3);
        String currentVarName = "PAR";
        tmp = tmp.substring(offset + currentVarName.length(), tmp.length()-2);
        rl.Str2JS(tmp, f.getName(), currentVarName);

        tmp = content.read(input_dir + f.getName(), 4, 4);
        currentVarName = "tempC";
        tmp = tmp.substring(offset + currentVarName.length(), tmp.length()-2);
        rl.Str2JS(tmp, f.getName(), "tempC");

    }
}
