/*
 * This class is used to generate JS file for graphing
 */
package bayesian;
import java.io.*;

/**
 *
 * @author Dong (Kevin) Zhang
 */
public class JSgen {
    private String jspath;
    
    public JSgen(String jsdata){
        this.jspath = jsdata;
    }
    
    public void CodingHTML(String csvName, String[] varList, int Nmeasurements){
        //Appending codes to existing copy of the template
        try {
            FileWriter html = new FileWriter(this.jspath + "/" + csvName + ".html");
            String codebody = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "    <head>\n"
                    + "        <title>Graphs from Bayesian Model</title>\n"
                    + "        <meta charset=\"UTF-8\">\n"
                    + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <div>Plots from Bayesian model output<br><hr></div>\n"
                    + "        <script src=\"jquery-3.1.1.min.js\" type=\"text/javascript\"></script>\n";
            String srcJSdata = "";
            for (String jsname : varList){
                if(!jsname.contains("DO"))
                    srcJSdata += "        <script src=\"jsdata/" + csvName + "_" + jsname + ".js\"></script>\n";
            }
            srcJSdata += "        <script src=\"jsdata/" + csvName + "_" + "counter.js\"></script>\n"
                    + "        <script src=\"jsdata/" + csvName + "_" + "obsID.js\"></script>\n";

            codebody += srcJSdata + "        <script src=\"Chart.bundle.js\"></script>\n"
                    + "        <script src=\"DrawMyChart.js\"></script>\n";
            
            String plots = "";
            // Include DO.models
            for(int m=1; m<=Nmeasurements; m++){
                plots += "        <script src=\"jsdata/" + csvName + "_DO.modelled[" + m + "].js\"></script>\n";
            }
            plots +=  "        <script src=\"stat.js\"></script>\n";
            
            for(String s : varList){
                String tmp = s.replace(".", "_");
                plots += "Plot of " + s
                        + "        <canvas id=\"myChart" + tmp + "\" width=\"800\" height=\"600\"></canvas>\n"
                        + "<hr>";    
            }
            codebody += plots + "    </body>\n"
                    + "</html>";
            html.write(codebody);
            html.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void codingJS(int nchains, String[] varList, String[] scatter){
        //Create DrawMyChart.js to draw the plots
        try {
            FileWriter js = new FileWriter(this.jspath + "/DrawMyChart.js");
           
            int color = 0;
            String head = "window.onload = function (){\n";
            String ds = "";
            
            // Trace plots of monitor variable
            for (String s : varList) {
                String tmp = s.replace(".", "_");
                ds += "    var ctx = document.getElementById(\"myChart" + tmp + "\").getContext(\"2d\");\n"
                        + "    var data = {\n"
                        + "    labels : labels,\n"
                        + "    datasets : [\n";;
                for (int j = 0; j < nchains - 1; j++) {
                    ds += "        {\n"
                            + "            data : " + tmp + ".chain" + (j + 1) + ",\n"
                            + "            label: \"Chain" + (j + 1) + "\",\n"
                            + "            pointBackgroundColor: \"rgba(75,192," + color + ",1)\",\n"
                            + "            fill: false\n"
                            + "        },\n";
                    color += 50;
                }
                ds += "        {\n"
                        + "            data : " + tmp + ".chain" + nchains + ",\n"
                        + "            label: \"Chain" + nchains + "\",\n"
                        + "            pointBackgroundColor: \"rgba(75,192," + color + ",1)\",\n"
                        + "            fill: false\n"
                        + "        }\n"
                        + "    ]};\n"
                        + "\n"
                        + "    var myLineChart = new Chart(ctx, {\n"
                        + "    type: 'line',\n"
                        + "    data: data\n"
                        + "    });\n\n\n";
            }
            
            // Coding sctter plots
            if(scatter != null){
                for (String t : scatter) {
                    String tmp = t.replace(".", "_");
                    ds += "    var ctx = document.getElementById(\"myChart" + tmp + "\").getContext(\"2d\");\n"
                            + "    var data = {\n"
                            + "    labels : obs,\n"
                            + "    datasets : [\n";;
                    
                    if(tmp.contains("DO")){
                        ds += "        {\n"
                                + "            data : DO_lower,\n"
                                + "            label: \" Lower fence \",\n"
                                + "            pointBackgroundColor: \"rgba(255, 0, 0,1)\",\n"
                                + "            fill: false\n"
                                + "        },\n"
                                + "        {\n"
                                + "            data : DO_upper,\n"
                                + "            label: \" Upper fence \",\n"
                                + "            pointBackgroundColor: \"rgba(0,255,0,1)\",\n"
                                + "            fill: false\n"
                                + "        },\n"
                                +"        {\n"
                                + "            data : DO_modeled_mean,\n"
                                + "            label: \" Average from Model \",\n"
                                + "            pointBackgroundColor: \"rgba(100,75," + color + ",1)\",\n"
                                + "            fill: false\n"
                                + "        }\n"
                                + "    ]};\n"
                                + "\n"
                                + "    var myLineChart = new Chart(ctx, {\n"
                                + "    type: 'line',\n"
                                + "    data: data\n"
                                + "    });\n\n\n";
                    }else{ 
                        ds += "        {\n"
                                + "            data : " + tmp + ",\n"
                                + "            label: \"" + tmp + "\",\n"
                                + "            pointBackgroundColor: \"rgba(200,75," + color + ",1)\",\n"
                                + "            fill: false\n"
                                + "        }\n"
                                + "    ]};\n"
                                + "\n"
                                + "    var myLineChart = new Chart(ctx, {\n"
                                + "    type: 'line',\n"
                                + "    data: data\n"
                                + "    });\n\n\n";
                    }
                }
            }
            
            String end = "};";
            js.write(head + ds + end);
            js.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void codingStatJS(int Nmeasurment, int nRow, int nChains){
        try{
            FileWriter statjs = new FileWriter(this.jspath + "/stat.js");
            String code = "var nSize = " + Nmeasurment + ";\n"
                    + "var nRow =" + nRow + ";\n"
                    + "var nChains = " + nChains + ";\n"
                    + "\n"
                    + "DO_modeled_data = new Array();\n"
                    + "for(i=0; i<nSize; i++){\n"
                    + "    DO_modeled_data[i] = [";
            
            for (int i = 1; i < nChains; i++) {
                code += "DO_modelled[i+1].chain" + i + ",";
            }
            code += "DO_modelled[i+1].chain" + nChains + "];\n";

            code += "}\n"
                    + "\n"
                    + "var DO_modeled_mean = new Array();\n"
                    + "for(i=0; i<nSize; i++){\n"
                    + "    var s = 0;\n"
                    + "    for(j=0; j<nChains; j++)\n"
                    + "        s += DO_modeled_data[i][j].reduce(function(x,y){return x+y});\n"
                    + "    DO_modeled_mean[i] = s/(nRow*nChains);\n"
                    + "}\n"
                    + "\n"
                    + "var DO_modeled_sd = new Array();\n"
                    + "for(i=0; i<nSize; i++){\n"
                    + "    var ss = 0;\n"
                    + "    for(j=0; j<nChains; j++)\n"
                    + "        ss += DO_modeled_data[i][j].map(function(x){return((x-DO_modeled_mean[i])**2)}).reduce(function(x,y){return x+y});\n"
                    + "    DO_modeled_sd[i] = ss/(nRow*nChains-1);\n"
                    + "}\n\n"
                    + "var DO_upper = new Array();\n"
                    + "var DO_lower = new Array();\n"
                    + "for(i=0; i<nSize; i++){\n"
                    + "    DO_upper[i] = DO_modeled_mean[i] + DO_modeled_sd[i];\n"
                    + "    DO_lower[i] = DO_modeled_mean[i] - DO_modeled_sd[i];\n"
                    + "}";
            statjs.write(code);
            statjs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
