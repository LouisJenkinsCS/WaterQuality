/*
 * Fitting the Bayesian model using JAGS
 * In the package, APIs of JAGS have been defined
 * 
 * Since Javascript will be used to draw the plots
 * instead of using Java graphics, the program will
 * generate the JS file to draw the plots.
 */
package bayesian;
import utilities.DataToCSV;
import async.Data;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static String file_name = "";
    private static int interval = 600; // IMO, this variable is used to generate instant_rate table.
    public static final long PAR = 637957793;
    public static final long HDO = 1050296639;
    public static final long Temp = 639121399;
    public static final long Pressure = 639121405;
    public static final double ATMOSPHERIC_CONVERSION_FACTOR = 0.000986923;

    private static String EXAMPLE = "";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        
        FileOperation fo = new FileOperation();
        
        Instant day = getFullDayOfData();
        LocalDateTime dt = LocalDateTime.ofInstant(day, ZoneId.systemDefault());
        file_name = dt.getYear() + "_" + dt.getMonthValue() + "_" + dt.getDayOfMonth() + ".csv";
        Data data = DataReceiver.getData(
                day,
                day.plus(Period.ofDays(1)),
                PAR, HDO, Temp, Pressure);
        
        System.out.println(DataToCSV.dataToCSV(data, true));
        ForkJoinPool.commonPool().execute(() -> runJJAGSForCSV(fo, data));
        
        ForkJoinPool.commonPool().awaitQuiescence(Integer.MAX_VALUE, TimeUnit.DAYS);
    }
    
    public static Instant getFullDayOfData() {
        Instant day = Instant
                .now()
                .truncatedTo(ChronoUnit.DAYS)
                .minus(Period.ofDays(1));
        
        int attempts = 1000;
        int currAttempts = 0;
        
        while (currAttempts++ < attempts) {
            long cnt = DataReceiver
                    .getData(day, day.minus(Period.ofDays(1)), PAR, HDO, Temp, Pressure)
                    .getData()
                    .subscribeOn(Schedulers.computation())
                    .count()
                    .blockingGet();
            
            if (cnt == 92 * 4) {
                return day.minus(Period.ofDays(1));
            } else {
                day = day.minus(Period.ofDays(1));
            }
        }
        
        throw new RuntimeException("Could not find a full day in " + attempts + " tries...");
    }
    
    public static String formatForDay(List<DataValue> values) {
        // Already sorted by timestamp in ascending order.
        Instant time = values.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS);
        Instant endTime = values.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS).plus(Period.ofDays(1));
        double lastValue = 0;
        List<Double> graphData = new ArrayList<>();
        for (DataValue value : values) {
            while(value.getTimestamp().compareTo(time) != 0 && time.compareTo(endTime) <= 0) {
                graphData.add(value.getValue());
                time = time.plus(Duration.ofMinutes(15));
            }
            
            graphData.add(value.getValue());
            lastValue = value.getValue();
        }
        
        return graphData.stream().map(Object::toString).collect(Collectors.joining(","));
    }
    
    
    private static void runJJAGSForCSV(FileOperation fo, Data data) {
        String fname = file_name;
        //Fitting the model and obtain the results in the folder output/
        String tmp_dir = base_dir + fname + "/";

        // Create the temporary folder for current CSV calculation
        fo.newFolder(tmp_dir);
        // Create the output folder for current CSV calculation inside output/
        fo.newFolder(output_dir + fname);

        csvReader content = new csvReader();
        String cnt = data.getData()
                .observeOn(Schedulers.computation())
                .groupBy((DataValue dv) -> dv.getId())
                .flatMap((GroupedObservable<Long, DataValue> group) -> {
                    String header;
                    if (group.getKey() == PAR) {
                        header = "PAR <- c(";
                    } else if (group.getKey() == HDO) {
                        header = "DO.meas <- c(";
                    } else if (group.getKey() == Temp) {
                        header = "tempC <- c(";
                    } else if (group.getKey() == Pressure) {
                        header = "atmo.pressure <- c(";
                    } else {
                        throw new RuntimeException("Bad Key: " + group.getKey());
                    }
                    
                    return group.sorted()
                                .map(dv -> dv.getId() == Pressure ? new DataValue(dv.getId(), dv.getTimestamp(), dv.getValue() * ATMOSPHERIC_CONVERSION_FACTOR) : dv)
                                .buffer(Integer.MAX_VALUE)
                                .map(RunBayesianModel::formatForDay)
                                .map(header::concat);
                })
                .map(str -> str + ")")
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .map(str -> str + "salinity <- c(" + IntStream.range(0, 96).map(x -> 0).mapToObj(Integer::toString).collect(Collectors.joining(",")) + ")")
                .blockingGet();;
        
        content.nRow = 92;
        // String cnt = content.read(input_dir + f.getName(), 3, 4);
        String datafile = "num.measurements <- c(" + content.nRow + ")\n"
                + "interval <- c(" + interval + ")\n"
                + cnt;
        fo.newFile(tmp_dir + "data.txt", datafile);

        // JAGS object
        // If you have path to jags in your PATH, you can use "jags" only without path to it.
        // ---- Windows JAGS ----
         JJAGS jg = new JJAGS("\"C:/Program Files/JAGS/JAGS-4.2.0/x64/bin/jags.bat\"", tmp_dir, fname, base_dir + model_name);
        // ---- Linux JAGS ----
//            JJAGS jg = new JJAGS("/usr/bin/jags", tmp_dir, base_dir + model_name);            

        // Variables to be monitored. They will be used to create graphs. You can read a config file to get them.
        String[] monitor = {"A","R","K","K.day","p","theta","sd","ER","GPP", "NEP","sum.obs.resid","sum.ppa.resid","PPfit","DO.modelled"};  
        String[] init = {""};   // Initial values to the model, can be blank.
        int nchains = 3, nthin = 10, niter = 20000, nburnin = (int) (niter * 0.5);
        jg.make_script(nchains, nthin, monitor, nburnin, niter);
        jg.make_init(123, init); // Random seed can be changed for different CSV files
        jg.jags_run();  // Here if the app running properly, you will get "Job done" in the terminal
        

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
        fo.copyFile(jsroot_dir + "Chart.bundle.js", output_dir + fname + "/" + "Chart.bundle.js");
        fo.copyFile(jsroot_dir + "jquery-3.1.1.min.js", output_dir + fname + "/" + "jquery-3.1.1.min.js");
        fo.newFolder(output_dir + fname + "/jsdata");

        resultLoader rl = new resultLoader(output_dir + fname);
        // Parsing all variables out from TXT outputs and create JS files for each
        rl.PrepareDataJS(content.nRow, nchains, niter, nthin, fname);
        // Generate the graph JS file
        String plotVarList = "A,R,p,K.day,theta"; // Do NOT place "spaces"!!!
        String scatterList = "PAR,tempC,DO.modelled";   // SAME as above!!!
        JSgen csvjs = new JSgen(output_dir + fname);
        csvjs.CodingHTML(fname, (plotVarList + "," + scatterList).split(","), content.nRow);
        csvjs.codingJS(nchains, plotVarList.split(","), scatterList.split(","));
        csvjs.codingStatJS(content.nRow, (int)(niter/nthin), nchains);

        // Scatter plots of PAR and tempC
        data
                .getData()
                .subscribeOn(Schedulers.computation())
                .filter(dv -> dv.getId() == PAR)
                .sorted()
                .buffer(Integer.MAX_VALUE)
                .map(RunBayesianModel::formatForDay)
                .blockingSubscribe(plotData -> rl.Str2JS(plotData, fname, "PAR"));
        
        data
                .getData()
                .subscribeOn(Schedulers.computation())
                .filter(dv -> dv.getId() == Temp)
                .sorted()
                .buffer(Integer.MAX_VALUE)
                .map(RunBayesianModel::formatForDay)
                .blockingSubscribe(plotData -> rl.Str2JS(plotData, fname, "tempC"));
    }
    
}
