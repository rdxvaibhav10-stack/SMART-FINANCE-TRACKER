package backend.service;

public class PredictionService {
    public double predictNext(double[] y) {
        int n = y.length;
        if (n == 0) return 0;
        if (n == 1) return y[0];
        double sumX=0,sumY=0,sumXY=0,sumXX=0;
        for (int i=0;i<n;i++) {
            double x = i+1;
            sumX+=x; sumY+=y[i]; sumXY+=x*y[i]; sumXX+=x*x;
        }
        double denom = (n*sumXX - sumX*sumX);
        double b = denom == 0 ? 0 : (n*sumXY - sumX*sumY) / denom;
        double a = (sumY - b*sumX)/n;
        return a + b*(n+1);
    }
}
