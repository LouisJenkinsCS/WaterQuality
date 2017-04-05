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
import database.DatabaseManager;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.simple.JSONObject;

/**
 *
 * @author Dong Zhang
 */
public class RunBayesianModel {
    
    // The hard-text version of the metabolism model; issue is that CSWebStage
    // places files in varying places.
    private static String metab_model = "\n" +
"             model\n" +
"                  {\n" +
"                    \n" +
"                    # PRIORS\n" +
"                    \n" +
"                    #-------------------------------------------------------------\n" +
"                    \n" +
"                    # (Y) Set treatment of K prior:\n" +
"                    \n" +
"                    # (Y1) K estimated simultaneously by model\n" +
"                    K ~ dnorm(0,0.25)I(0,) 	\n" +
"                    \n" +
"                    \n" +
"                    # (Y2) K mean and uncertainty measured\n" +
"                    # measured.k <- 10 # your estimate of k (example of 10)\n" +
"                    # measured.k.sd <- 0.0001   # your estimate of standard deviation of k (example of 0.0001 - this will give an essentially known and fixed k)\n" +
"                    # kf<- measured.k / (seconds / interval)	\n" +
"                    # k.prec <- 1/(measured.k.sd*measured.k.sd)\n" +
"                    # K ~ dnorm(kf,k.prec)I(0,)\n" +
"                    \n" +
"                    \n" +
"                    #-------------------------------------------------------------\n" +
"                    \n" +
"                    # (Z) Set estimation of theta and p parameters:\n" +
"                    \n" +
"                    # (Z1) theta and p (effectively) fixed - xx just makes them stochastic\n" +
"                    p <- 1.0    \n" +
"                    theta <- 1.07177\n" +
"\n" +
"                    # (Z2) treat theta and p as estimable parameters\n" +
"                    # theta ~ dnorm(0.0,0.25)I(1.0,1.3)\n" +
"                    # p ~ dnorm(0.0,0.25)I(0.3,1.0)\n" +
"                   \n" +
"                                 \n" +
"\n" +
"                    #-------------------------------------------------------------\n" +
"                    ## DO NOT ALTER BELOW HERE\n" +
"\n" +
"                    A ~ dnorm(0,0.25)I(0,)\n" +
"                    R ~ dnorm(0,0.25)I(0,)	\n" +
"                    \n" +
"                    sd ~ dunif(0.001,0.2); tau <- 1/(sd*sd)\n" +
"                    \n" +
"                    seconds <- 24 * 60 * 60\n" +
"                    \n" +
"		    # NEW alternate PI parameterisation\n" +
"                    #P1 ~ dnorm(0,0.001)I(0,)\n" +
"                    #P2 ~ dnorm(0, 0.01)I(0,)\n" +
"\n" +
"                    for (i in 1:num.measurements)\n" +
"                          {\n" +
"                          kelvin[i] <- 273.15 + tempC[i]\n" +
"                          \n" +
"                          # correction for salinity\n" +
"                          \n" +
"                          S1[i] <- 157570.1 / kelvin[i]\n" +
"                          S2[i] <- -6.6423080E7 / (kelvin[i] * kelvin[i])\n" +
"                          S3[i] <- 1.2438E10 / pow(kelvin[i],3)\n" +
"                          S4[i] <-  -8.621949E11 / pow(kelvin[i],4)\n" +
"                          sal.factor[i] <- -1.0*salinity[i]*(0.017674-10.754/kelvin[i]+2140.7/(kelvin[i]*kelvin[i]))\n" +
"                          \n" +
"                          DOsalinity.corr[i] <-exp(-139.34411+S1[i]+S2[i]+S3[i]+S4[i]+sal.factor[i])\n" +
"                          \n" +
"                          # correction for atmospheric pressure\n" +
"                          alpha[i] <- 0.000975-0.00001426*kelvin[i]+0.00000006436*pow(kelvin[i],2)		\n" +
"                          beta[i] <- exp(11.8571-3840.7/kelvin[i]-216961/pow(kelvin[i],2))\n" +
"                          gamma[i] <- ((1-beta[i]/atmo.pressure[i]) / (1-beta[i])) * ((1-alpha[i]*atmo.pressure[i])/(1-alpha[i]))\n" +
"                          \n" +
"                          DO.sat[i] <- DOsalinity.corr[i]*atmo.pressure[i]*gamma[i]		\n" +
"                                                         			  \n" +
"                          }\n" +
"                    \n" +
"                    temp.ave <- mean(tempC[])\n" +
"                    \n" +
"                    Aprime <- A # no cut in jags\n" +
"                    pprime <- p # no cut in jags\n" +
"                    gppts[num.measurements] <- 0 #updated\n" +
"\n" +
"                    # for posterior predictive assessment and goodness of fit evaluation\n" +
"                    obs.resid[num.measurements] <- 0\n" +
"                    ppa.resid[num.measurements] <- 0\n" +
"                    \n" +
"                    # DO modelled\n" +
"                    zz ~ dnorm(0,1000000)\n" +
"                    DO.modelled[1] <- DO.meas[1] + zz # make monitorable\n" +
"                    \n" +
"                    \n" +
"                    for (i in 1:(num.measurements-1))\n" +
"                            {		\n" +
"                            # estimating primary production\n" +
"                            gppts[i] <- Aprime*pow(PAR[i],pprime) # Grace et al 2015\n" +
"                            # gppts[i] <- PAR[i] / (P1 + P2*PAR[i]) # alternate parameterisation\n" +
"                                                       \n" +
"                            DO.meas[i+1] ~ dnorm(DO.modelled[i+1], tau)\n" +
"                            DO.modelled[i+1] <- DO.modelled[i] +\n" +
"					A * pow(PAR[i],p) # Grace et al 2015 parameterisation\n" +
"                                      			#  PAR[i] / (P1 + P2*PAR[i-1]) # alternate parameterisation\n" +
"                                      	- R * (pow(theta,(tempC[i]-temp.ave)))\n" +
"                                      	+ K * (DO.sat[i]-DO.modelled[i]) * pow(1.0241,(tempC[i]-temp.ave))\n" +
"                            \n" +
"\n" +
"                            # posterior predictive assessment nodes #\n" +
"                                                       \n" +
"                            # plausible sampled value\n" +
"                            DO.sampled.fit[i] ~ dnorm(DO.modelled[i], tau) \n" +
"                                                        \n" +
"                            # squared residuals		\n" +
"                            obs.resid[i] <- pow((DO.meas[i] - DO.modelled[i]),2)\n" +
"                            ppa.resid[i] <- pow((DO.sampled.fit[i] - DO.modelled[i]),2)\n" +
"                            \n" +
"                            }\n" +
"                    \n" +
"                    # for posterior predictive assessment and goodness of fit evaluation\n" +
"                    sum.obs.resid <- sum(obs.resid[])\n" +
"                    sum.ppa.resid <- sum(ppa.resid[])\n" +
"                    PPfit <- step(sum.obs.resid - sum.ppa.resid)\n" +
"                    \n" +
"                    ## Useful calculations\n" +
"                    ER <- (R * seconds) / interval\n" +
"                    GPP <- sum(gppts[])\n" +
"                    NEP <- GPP - ER\n" +
"                    K.day <- (K * seconds)/ interval\n" +
"                    \n" +
"                    }\n" +
"          \n" +
" \n" +
"";
    
    /* 
    * Some arguments will be passed to here using args: args[1] is model text file. 
    * Or you can use a confid file but involving File IO Stream here.
    */
    private static String model_name = "BASE_metab_model_v2.1.txt";
//    private static String user_dir = RunBayesianModel.class.getResource(".").getPath().substring(1);
//    private static String base_dir = user_dir + "BASE\\";
//    private static String input_dir = base_dir + "input\\"; 
//    private static String output_dir = base_dir + "output\\";
//    private static String jsroot_dir = base_dir + "JSchart\\";
    private static String file_name = "";
    private static int interval = 900; // IMO, this variable is used to generate instant_rate table.
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
        System.out.println(day.minus(Period.ofDays(1)).toEpochMilli());
        LocalDateTime dt = LocalDateTime.ofInstant(day, ZoneId.systemDefault());
        file_name = dt.getYear() + "_" + dt.getMonthValue() + "_" + dt.getDayOfMonth() + ".csv";
        System.out.println(file_name);
        Data data = DataReceiver.getRemoteData(
                day,
                day.plus(Period.ofDays(1)),
                PAR, HDO, Temp, Pressure);
        
        try {
            //        System.out.println(DataToCSV.dataToCSV(data, true));
            runJJAGSForCSV(fo, data);
        } catch (IOException ex) {
            Logger.getLogger(RunBayesianModel.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    .getRemoteData(day, day.minus(Period.ofDays(1)), PAR, HDO, Temp, Pressure)
                    .getData()
                    .count()
                    .blockingGet();
            
            if (cnt == 96 * 4) {
                return day.minus(Period.ofDays(1));
            } else {
                day = day.minus(Period.ofDays(1));
            }
        }
        
        DatabaseManager.LogError("Could not find a full day in " + attempts + " tries...");
//        throw new RuntimeException("Could not find a full day in " + attempts + " tries...");
        return null;
    }
    
    
    public static String formatForDay(List<DataValue> values) {
        // Already sorted by timestamp in ascending order.
        Instant time = values.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS);
        Instant endTime = values.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS).plus(Period.ofDays(1));
        double lastValue = Double.NaN;
        List<Double> graphData = new ArrayList<>();
        if (values.isEmpty()) {
            return IntStream.range(0, 96).mapToDouble(n -> Double.NaN).mapToObj(Double::toString).collect(Collectors.joining(","));
        }
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
    
    public static void LogException(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        DatabaseManager.LogError("Exception: " + ex.getClass().getName() + "\nStack Trace: " + errors.toString());
//        throw new RuntimeException(ex);
    }
    
    public static Observable<JSONObject> trialJAGS(Instant day) {
//        LocalDateTime dt = LocalDateTime.ofInstant(day, ZoneId.systemDefault());
//        file_name = dt.getYear() + "_" + dt.getMonthValue() + "_" + dt.getDayOfMonth() + ".csv";
        DatabaseManager.LogError("Insider of trialJAGS...");
        Data data = DataReceiver.getRemoteData(
                day,
                day.plus(Period.ofDays(1)),
                PAR, HDO, Temp, Pressure);
         DatabaseManager.LogError("Obtained deferred remote data...");
        
        try {
            //        System.out.println(DataToCSV.dataToCSV(data, true));
            return runJJAGSForCSV(new FileOperation(), data);
        } catch (IOException ex) {
            LogException(ex);
            return null;
        }
    }
    
    public static Observable<JSONObject> runJJAGSForCSV(FileOperation fo, Data data) throws IOException {
        // Create the base directories needed as temporaries.
        Path base_directory = Files.createTempDirectory("BASE");
        Path temporary_directory = Files.createTempDirectory(base_directory, "BayesianModelData");
        Path output_directory = Files.createTempDirectory(base_directory, "output");
//        String fname = file_name;
//        //Fitting the model and obtain the results in the folder output/
//        String tmp_dir = base_dir + fname + "/";

        // Create the temporary folder for current CSV calculation
//        fo.newFolder(tmp_dir);
//        // Create the output folder for current CSV calculation inside output/
//        fo.newFolder(output_dir + fname);

        csvReader content = new csvReader();
        String cnt = data.getData()
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
                                .defaultIfEmpty(new ArrayList<>())
                                .map(RunBayesianModel::formatForDay)
                                .map(header::concat);
                })
                .map(str -> str + ")")
                .reduce("", (str1, str2) -> str1 + str2 + "\n")
                .map(str -> str + "salinity <- c(" + IntStream.range(0, 96).map(x -> 0).mapToObj(Integer::toString).collect(Collectors.joining(",")) + ")")
                .blockingGet();;
        
        content.nRow = 96;
        // String cnt = content.read(input_dir + f.getName(), 3, 4);
        String datafile = "num.measurements <- c(" + content.nRow + ")\n"
                + "interval <- c(" + interval + ")\n"
                + cnt;
        
        Path temp_data = Files.createTempFile(temporary_directory, "data", ".txt");
        FileWriter writer = new FileWriter(temp_data.toFile());
        writer.append(datafile);
        writer.close();
//      fo.newFile(tmp_dir + "data.txt", datafile);
        Path temp_model = Files.createTempFile(base_directory, "BASE_metab_model_v2.1", ".txt");
        writer = new FileWriter(temp_model.toFile());
        writer.append(metab_model);
        writer.close();
        
        // JAGS object
        // If you have path to jags in your PATH, you can use "jags" only without path to it.
        // ---- Windows JAGS ----
        // Local: C:/Users/lpj11535/AppData/Local/JAGS/JAGS-4.2.0/x64/bin/jags.bat
         JJAGS jg = new JJAGS("\"C:/Users/lpj11535/AppData/Local/JAGS/JAGS-4.2.0/x64/bin/jags.bat\"", temporary_directory.toString(), "BayesianModelData", temp_model.toString(), temp_data.toString(), output_directory.toString());
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
//        fo.copyFile(jsroot_dir + "Chart.bundle.js", output_dir + fname + "/" + "Chart.bundle.js");
//        fo.copyFile(jsroot_dir + "jquery-3.1.1.min.js", output_dir + fname + "/" + "jquery-3.1.1.min.js");
//        fo.newFolder(output_dir + fname + "/jsdata");

        
        DatabaseManager.LogError("Loading results from path: " + Paths.get(output_directory + "/BayesianModelData"));
        resultLoader rl = new resultLoader(output_directory);
        return rl.parseJAGSOutput(content.nRow, nchains, niter, nthin);
//        // Parsing all variables out from TXT outputs and create JS files for each
//        rl.PrepareDataJS(content.nRow, nchains, niter, nthin, fname);
//        // Generate the graph JS file
//        String plotVarList = "A,R,p,K.day,theta"; // Do NOT place "spaces"!!!
//        String scatterList = "PAR,tempC,DO.modelled";   // SAME as above!!!
//        JSgen csvjs = new JSgen(output_dir + fname);
//        csvjs.CodingHTML(fname, (plotVarList + "," + scatterList).split(","), content.nRow);
//        csvjs.codingJS(nchains, plotVarList.split(","), scatterList.split(","));
//        csvjs.codingStatJS(content.nRow, (int)(niter/nthin), nchains);
//
//        // Scatter plots of PAR and tempC
//        data
//                .getData()
//                .subscribeOn(Schedulers.computation())
//                .filter(dv -> dv.getId() == PAR)
//                .sorted()
//                .buffer(Integer.MAX_VALUE)
//                .map(RunBayesianModel::formatForDay)
//                .blockingSubscribe(plotData -> rl.Str2JS(plotData, fname, "PAR"));
//        
//        data
//                .getData()
//                .subscribeOn(Schedulers.computation())
//                .filter(dv -> dv.getId() == Temp)
//                .sorted()
//                .buffer(Integer.MAX_VALUE)
//                .map(RunBayesianModel::formatForDay)
//                .blockingSubscribe(plotData -> rl.Str2JS(plotData, fname, "tempC"));
    }
    
}
