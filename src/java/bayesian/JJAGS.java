/*
 * Define JAGS APIs in Java
 * Everything of the Bayesian model is here
 * jags needs following files: data, model, init, and a script
 * The outputs will be CODAindex and CODAchain files
 */
package bayesian;
import database.DatabaseManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author Dong Zhang
 */
public class JJAGS {
    // Settings of the model
    private String script_file, data_file, init_file, mod_file;
    private String tmp_dir;
    private String dir_name;
    private String out_dir;
    String jags_bin;
    
    public JJAGS(String jagsbin, String tmpdir, String dir_name, String modelfile, String dataFile, String outdir){
        // Ontaining installation path of JAGS
        this.jags_bin = jagsbin;
        this.tmp_dir = tmpdir;
        this.dir_name = dir_name;
        this.script_file = this.tmp_dir + "/script.txt";
        this.data_file = dataFile;
        this.init_file = this.tmp_dir + "/init.txt";
        this.out_dir = outdir;
        //this.mod_file = this.tmp_dir + "model.txt";
        this.mod_file = modelfile;
        //System.out.println(this.script_file);
    }
    
    // Create data file
    
    // Create init file
    public void make_init(int seed, String[] initVars){
        try {
            FileWriter writer = new FileWriter(this.init_file);
            String init = "\".RNG.name\" <- \"base::Super-Duper\"\n" 
                    + "\".RNG.seed\" <- " + seed + "\n";
            for( String var : initVars){
                init += var + "\n"; 
            }

            //System.out.println(init);
            writer.write(init);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Create script file
    public void make_script(int nchains, int nthin, String[] monitorVars, int nburnin, int niter){
        try {
            FileWriter writer = new FileWriter(this.script_file);
            String mon = "";
            for( String var : monitorVars){
                mon += "monitor " + var + ", thin(" + nthin + ")\n"; 
            }
            
//            System.out.println(JJAGS.class.getResource("BASE/output/" + dir_name).getPath().substring(1));
//            System.exit(0);
            
            
            String src = "model clear\n"
                    + "data clear\n"
                    + "model in \"" + this.mod_file + "\"\n"
                    + "data in \"" + this.data_file + "\"\n"
                    + "compile, nchains(" + nchains + ")\n"
                    + "inits in \"" + this.init_file + "\"\n"
                    + "initialize\n"
                    + "update "+ nburnin +"\n"
                    + mon 
                    + "update " + niter + "\n"
                    + "cd \"" + out_dir + "\"\n"
                    + "coda *";
            DatabaseManager.LogError(src);
            //System.out.println(src);
            writer.write(src);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    // Execute JAGS, fitting the model; There are x84 and x64 versions of JAGS
    public void jags_run(){
        // Run JAGS: 
        // JAGS running the model chain by chain, thus more chains takes longer
        // Using multiple threads may solve the issue???
        String cmd = this.jags_bin + " " + script_file;
        DatabaseManager.LogError(cmd);
//        System.out.println(Paths.get(script_file.substring(0, script_file.indexOf("/")) + "\\").toFile().exists());
        System.out.println("JAGS is running, be patient please...");
        try {
            Path out = Files.createTempFile("jags-out", null);
            ProcessBuilder pb = new ProcessBuilder()
                    .redirectError(out.toFile())
                    .command(this.jags_bin, script_file);            
            
            DatabaseManager.LogError("Current Path: " + pb.directory());
            System.out.println("User Path: " + System.getProperty("user.dir"));
//            System.exit(0);
            Process p = pb.start();
            
            p.waitFor();
            
            DatabaseManager.LogError("JAGS Output: " + new BufferedReader(
                    new FileReader(out.toFile())
                ).lines().collect(Collectors.joining("\n"))
            );
            int i = p.exitValue();
            if (i == 0) {
                System.out.println("Job done!");
            } else {
                System.out.println("Oops! Job failed...");
            }
            p.destroy();
            p = null;
        } catch (Exception e) {
            RunBayesianModel.LogException(e);
        }
    }
}
