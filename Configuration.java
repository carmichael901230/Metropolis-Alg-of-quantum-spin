import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;

class Configuration {
    public int N=100;          // Number of spins
    public double B, C, T;
    public static int Nf;          // perform Nf*N flips 
    public int[] sigma;
    
    public Configuration(int N, double B, double C, double T, int Nf) {
        this.N = N;
        this.B = B;
        this.C = C;
        this.T = T;
        Configuration.Nf = Nf;

        if (C>=0) {
            this.sigma = new int[this.N];
            for (int i=0; i<this.N; i++) {
                this.sigma[i] = 1;
            }
        }
        else {
            this.sigma = new int[this.N];
            for (int i=0; i<this.N; i++) {
                if (i%2==0) this.sigma[i] = 1;
                else this.sigma[i] = -1;
            }
        }
    }
    public Configuration(double B, double C, double T) {
        this.B = B;
        this.C = C;
        this.T = T;

        if (C>=0) {
            this.sigma = new int[N];
            for (int i=0; i<this.N; i++) {
                this.sigma[i] = 1;
            }
        }
        else {
            this.sigma = new int[N];
            for (int i=0; i<this.N; i++) {
                if (i%2==0) this.sigma[i] = 1;
                else this.sigma[i] = -1;
            }
        }
    }

    // Generate new sigma by flip the spin at given [index]
    // Return new [sigma]
    public int[] createNewSigma(int index) {
        int[] sigma_new = this.sigma.clone();
        sigma_new[index] *= -1;
        return sigma_new;
    }

    // compute difference between oldSigma and newSigma
    // return sigma difference between newSigma and oldSigma
    public double calcSigmaDiff(int i) {
        // calculate delta B and C
        double deltaB = this.sigma[i]*2.0;
        double deltaC = 2.0*(this.sigma[(i-1+this.N)%this.N]*this.sigma[i]+this.sigma[i]*this.sigma[(i+1)%this.N]);
        return this.B*deltaB+this.C*deltaC;
    }

    // calculate and return [p]
    public double computeP(double diff) {
        return Math.exp((-1 * diff) / this.T);
    }

    // Wrapped function, flip Nf*N times
    // 1) generate a sigma configuration
    // 2) calculate delta energy
    // 3) calculate p and compare with random number r
    // 4) update sigma configuration
    public void flipSigma() {
        int[] newSigma;
        for (int i=0; i<Configuration.Nf*this.N; i++) {
            // Random generate an index
            int index = ThreadLocalRandom.current().nextInt(this.N);
            // Generate new sigma by flip [index] spin
            newSigma = this.createNewSigma(index);
            // calculate delta energy
            double deltaE = this.calcSigmaDiff(index);
            // Apply Metropolis Alg
            if (deltaE < 0){
                this.sigma = newSigma;
            }
            else {
                double p = this.computeP(deltaE);
                double r = ThreadLocalRandom.current().nextDouble(1.0);
                if (r < p) {
                    this.sigma = newSigma;
                }
            }
        }
    }

    public double computeM() {
        int sum = 0;
        for (int i=0; i<this.N; i++) {
            sum += this.sigma[i];
        }
        return sum*1.0/this.N;
    }
    public double computeCp() {
        int sum = 0;
        for (int i=0; i<this.N; i++) {
            sum += this.sigma[i]*this.sigma[(i+1)%this.N];
        }
        return sum*1.0/this.N;
    }

    public static double computeCpStar(){
        double C=-1.0, T=1.9;
        return (Math.exp(C/T)-Math.exp(-1*C/T)) / (Math.exp(C/T)+Math.exp(-1*C/T));
    }
}