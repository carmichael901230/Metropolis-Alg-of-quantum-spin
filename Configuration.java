import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;

class Configuration {
    public int N=100;          // Number of spins
    public double B, C, T;
    int[] sigma;
    
    public Configuration(int N, double B, double C, double T) {
        this.N = N;
        this.B = B;
        this.C = C;
        this.T = T;

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
        int deltaB = this.sigma[i]*-2;
        int deltaC = -2*(this.sigma[(i-1+this.N)%this.N]*this.sigma[i]+this.sigma[i]*this.sigma[(i+1)%this.N]);
        System.out.println("dB: "+deltaB);  //test
        System.out.println("dC: "+deltaC);  //test
        return this.B*deltaB+this.C*deltaC;
    }

    // calculate and return [p]
    public double computeP(double diff) {
        return Math.exp((-1 * diff) / this.T);
    }

    // Wrapped function
    // 1) generate a sigma configuration
    // 2) calculate delta energy
    // 3) calculate p
    // 4) compare with random number r and update sigma configuration
    public void updateCurrentSigma() {
        // Random generate an index
        int index = ThreadLocalRandom.current().nextInt(this.N);
        System.out.println("random index: "+index);     // test
        // Generate new sigma by flip [index] spin
        int[] newSigma = this.createNewSigma(index);
        // calculate delta energy
        double deltaE = this.calcSigmaDiff(index);
        System.out.println("Delta E: "+ deltaE);    //test
        // Apply Metropolis Alg
        if (deltaE < 0){
            this.sigma = newSigma.clone();
        }
        else {
            double p = this.computeP(deltaE);
            double r = ThreadLocalRandom.current().nextDouble(1.0);
            System.out.println("p: "+p);    // test
            System.out.println("random r: "+ r);  //test
            if (r < p) {
                this.sigma = newSigma.clone();
            }
        }
        newSigma = null;           // wait for garbage collection
    }

    public double computeM() {
        int sum = 0;
        for (int e : this.sigma) {
            sum += e;
        }
        return sum*1.0/this.N;
    }
    public double computeCp() {
        int sum = 0;
        for (int i=0; i<this.sigma.length; i++) {
            sum += this.sigma[i]*this.sigma[(i+1)%this.N];
        }
        return sum*1.0/this.N;
    }
}