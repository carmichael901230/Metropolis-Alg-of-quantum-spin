import java.util.ArrayList;
import java.util.concurrent.*;

public class Mthread implements Runnable {
    private static Semaphore mutex = new Semaphore(1);          // mutex of [globalM] and [globalC]
    public static Semaphore finished = new Semaphore(0);        // indicate all threads have finished
    private Thread t;
    private static double B, C, T;             // features of configuration
    private static int N;                      // features of configuration
    public static int Nt;          // number of threads
    public static int Nm;          // number of times Metropolis Alg is called
    public static int Nf;          // number of flips of each Metropolis alg

    private String threadName;

    public static double globalMeanM = 0;        // final result of M
    public static double globalMeanC = 0;        // final result of Cp

    public static ArrayList<Double> globalM = new ArrayList<>();          // record global mean of M, shared by all threads
    public static ArrayList<Double> globalC = new ArrayList<>();          // record global mean of Cp, shared by all threads
    
    public Mthread(int N, double B, double C, double T, int Nm, int Nt, int Nf, int i) {
        Mthread.Nt = Nt;         // total number of threads
        Mthread.Nm = Nm;         // number of times Metropolis is called in a thread
        // set features of configuration
        Mthread.N = N;  
        Mthread.B = B;
        Mthread.C = C;
        Mthread.T = T;
        Mthread.Nf = Nf;
        this.threadName = "Thread " + i;
    }

    public void start() {
        if (t == null) {
            t = new Thread (this);
            t.start();
        }
    }

    // call Metropolis Alg Nm times
    public void run() {
        double sumM = 0, sumC = 0;
        for (int i=0; i<Mthread.Nm; i++) {
            Configuration temp = new Configuration(Mthread.N, Mthread.B, Mthread.C, Mthread.T, Mthread.Nf);
            temp.flipSigma();
            // sum-up the [m] and [cp] for this configuration
            sumM += temp.computeM();
            sumC += temp.computeCp();
        }
        // wcalculate average of M and Cp and stroe them in global array
        // using mutex to force mutual exclusion
        try {
            Mthread.mutex.acquire();
                Mthread.globalM.add(sumM/(Mthread.Nm*1.0));
                Mthread.globalC.add(sumC/(Mthread.Nm*1.0));
            Mthread.mutex.release();
        }catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // after all threads are finished, calculate global mean of m and cp
        if (Mthread.globalM.size() == Mthread.Nt && Mthread.globalC.size() == Mthread.Nt){
            for (int i=0; i<Mthread.Nt; i++) {
                globalMeanM += globalM.get(i);
                globalMeanC += globalC.get(i);
            }
            globalMeanM /= (Mthread.Nt*1.0);
            globalMeanC /= (Mthread.Nt*1.0);
            Mthread.finished.release();
        }

    }
}