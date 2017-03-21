var nSize = 144;
var nRow =2000;
var nChains = 3;

DO_modeled_data = new Array();
for(i=0; i<nSize; i++){
    DO_modeled_data[i] = [DO_modelled[i+1].chain1,DO_modelled[i+1].chain2,DO_modelled[i+1].chain3];
}

var DO_modeled_mean = new Array();
for(i=0; i<nSize; i++){
    var s = 0;
    for(j=0; j<nChains; j++)
        s += DO_modeled_data[i][j].reduce(function(x,y){return x+y});
    DO_modeled_mean[i] = s/(nRow*nChains);
}

var DO_modeled_sd = new Array();
for(i=0; i<nSize; i++){
    var ss = 0;
    for(j=0; j<nChains; j++)
        ss += DO_modeled_data[i][j].map(function(x){return((x-DO_modeled_mean[i])**2)}).reduce(function(x,y){return x+y});
    DO_modeled_sd[i] = ss/(nRow*nChains-1);
}

var DO_upper = new Array();
var DO_lower = new Array();
for(i=0; i<nSize; i++){
    DO_upper[i] = DO_modeled_mean[i] + DO_modeled_sd[i];
    DO_lower[i] = DO_modeled_mean[i] - DO_modeled_sd[i];
}