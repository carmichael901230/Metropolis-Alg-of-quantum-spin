import java.util.ArrayList;
import java.util.Scanner;
import java.util.Scanner;

class Main {

    public static void main(String[] args) {
        // ===== Part 1 =====
        // Automatically determine value of Nf and Nm
        Part1();        

        // ===== Part 2 =====
        // Input B, C, T. Compute <cp> and <m> 
        // Part2();         

        // ===== Part 3 =====
        // Find peak of <m> 
        // Automatically find value of B and C, 
        // and compute <m> with respect of different value of T
        // Part3(); 
        
        // ===== Part 4 =====
        // Find vally of <cp>
        // Automatically find value of B and C, 
        // and compute <m> with respect of different value of T
        // Part4();
    }

    public static void Part1() {
        int Nf = determineNf();
        System.out.println("Nf = " + Nf);
        int Nm = determineNm();
        System.out.println("Nm = "+ Nm);
    }

    public static void Part2() {
        int Nf = 10, Nm = 100;
        // get value of B, C, T from user input
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter [B]: ");
        double B=sc.nextDouble();
        System.out.print("Enter [C]: ");
        double C=sc.nextDouble();
        System.out.print("Enter [T]: ");
        double T=sc.nextDouble();
        sc.close();
        System.out.println("Computation in Progress...");
        computeMeans(B, C, T,Nm, Nf);
    }

    public static void Part3() {
        double[] B_C = determineBC_m();
        findPeak(B_C[0], B_C[1]);
    }

    public static void Part4() {
        double[] B_C = determineBC_cp();      // determine B and C
        findMin(B_C[0], B_C[1]);       // find peak using given B, C values
    }

    public static void computeMeans(double B,double C, double T,int Nm, int Nf) {
        int N = 100;
        int Nt = 1000;

        ArrayList<Mthread> threadList = new ArrayList<>();
        for (int i=0; i<Nt; i++) {
            threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
        }
        for (int i=0; i<Nt; i++) {
            threadList.get(i).start();
        }

        try {
            Mthread.finished.acquire();
            // Print results <m> <cp>
            System.out.println("B="+B+" C="+C+ " T="+T+ " Nf="+Nf+ " Nm="+Nm);
            System.out.println("<m>=" + threadList.get(0).globalMeanM + " <cp>=" + threadList.get(0).globalMeanC);
        }catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        threadList.clear();
        Mthread.globalC.clear();
        Mthread.globalM.clear();
    }

    public static double computeRE(int Nf) {
        double B = 0, C = -1, T = 1.9;
        int N = 100;
        int Nt = 1000;
        // Nm and Nf are determined by testing relativeError and variance
        // See Excel file in same folder for details
        int Nm = 10;

        ArrayList<Mthread> threadList = new ArrayList<>();
        for (int i=0; i<Nt; i++) {
            threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
        }
        for (int i=0; i<Nt; i++) {
            threadList.get(i).start();
        }
        double cpStar = Configuration.computeCpStar();
        double relativeErr = 0.0;
        try {
            Mthread.finished.acquire();
            for (int i=0; i<Nt; i++) {
                relativeErr += (Mthread.globalC.get(i)-cpStar)/cpStar;
            }
            relativeErr /= Nt;
        }catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // Print result
        System.out.println("Nf="+Nf + " Nm="+Nm + " Relative Error="+relativeErr);
        threadList.clear();
        Mthread.globalC.clear();
        Mthread.globalM.clear();
        return relativeErr;
    }


    public static int determineNf() {
        System.out.println("Nf Calculation in Progress...");
        double relativeErr = 100;
        int Nf=0;
        // I changed |rc| value, because |rc|<0.02 always gives small Nf
        while (Math.abs(relativeErr)>=0.005) {
            Nf+=1;
            relativeErr = computeRE(Nf);
        }
        return Nf;
    }

    public static double computeVar(int Nm) {
        double B = 0, C = -1, T = 1.9;
        int N = 100;
        int Nt = 1000;
        // Nm and Nf are determined by testing relativeError and variance
        // See Excel file in same folder for details
        int Nf = 10;

        ArrayList<Mthread> threadList = new ArrayList<>();
        for (int i=0; i<Nt; i++) {
            threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
        }
        for (int i=0; i<Nt; i++) {
            threadList.get(i).start();
        }
        double cpStar = Configuration.computeCpStar();
        double relativeErr = 0.0, variance = 0.0;
        try {
            Mthread.finished.acquire();
            for (int i=0; i<Nt; i++) {
                relativeErr += (Mthread.globalC.get(i)-cpStar)/cpStar;
            }
            relativeErr /= Nt;
            for (int i=0; i<Nt; i++) {
                variance += Math.pow((Mthread.globalC.get(i)-cpStar)/cpStar - relativeErr, 2);
            }
        }catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        variance /= Nt;
        // Print result
        System.out.println("Nm="+Nm + " Nf="+Nf + " Variance="+variance);
        threadList.clear();
        Mthread.globalC.clear();
        Mthread.globalM.clear();
        return variance;
    }

    public static int determineNm() {
        System.out.println("Nm Calculation in Progress...");
        double SD = 100;
        int Nm=0;
        // I changed |SD| value, because |SD|<0.02 always gives small Nm
        while (Math.abs(SD)>=0.02) {
            Nm+=10;
            SD = Math.sqrt(computeVar(Nm));
        }
        return Nm;
    }

    public static double[] determineBC_m() {
        System.out.println("Seeking For Value of B, C, Making <m>=0...");
        double B=0.5, C=-0.001, T=0.01;
        int N = 100;
        int Nt = 1000;
        // use small Nm and Nf to speed up performace
        int Nm = 50;        
        int Nf = 8;
        outerloop: 
        for (; B<=2.5; B+=0.1) {
            for (; C>=-2.5; C-=0.1) {
                ArrayList<Mthread> threadList = new ArrayList<>();
                for (int i=0; i<Nt; i++) {
                    threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
                }
                for (int i=0; i<Nt; i++) {
                    threadList.get(i).start();
                }
        
                try {
                    Mthread.finished.acquire();
                    // System.out.println("B="+B+" C="+C+" <m>=" + threadList.get(0).globalMeanM);
                    if (Math.abs(threadList.get(Nt-1).globalMeanM)<=1E-10) {
                        System.out.println("Valid Value Found");
                        System.out.println("B="+B+" C="+C+" <m>=" + threadList.get(0).globalMeanM);
                        threadList.clear();
                        Mthread.globalC.clear();
                        Mthread.globalM.clear();
                        break outerloop;
                    }
                }catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                threadList.clear();
                Mthread.globalC.clear();
                Mthread.globalM.clear();
            }
            C=-0.001;        // reset C after inner loop
        }
        double[] result = {B,C};
        return result;
    }

    public static void findPeak(double B, double C) {
        double T=0.01;
        int N = 100;
        int Nt = 1000;
        // use small Nm and Nf to speed up performace
        int Nm = 50;        
        int Nf = 8;
        System.out.println("Exihibiting Peak at T>0 ...");
        for (; T<2; T+=0.08) {
            ArrayList<Mthread> threadList = new ArrayList<>();
            for (int i=0; i<Nt; i++) {
                threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
            }
            for (int i=0; i<Nt; i++) {
                threadList.get(i).start();
            }
    
            try {
                Mthread.finished.acquire();
                System.out.println("B="+B + " C="+ C +" T="+T + " <m>="+threadList.get(0).globalMeanM);
            }catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            threadList.clear();
            Mthread.globalC.clear();
            Mthread.globalM.clear();
        }
    }

    public static double[] determineBC_cp() {
        System.out.println("Seeking For Value of B, C. Making <cp>=1...");
        double B=0.8, C=-0.3, T=0.01;
        int N = 100;
        int Nt = 1000;
        // use small Nm and Nf to speed up performace
        int Nm = 50;        
        int Nf = 8;
        outerloop: 
        for (; B<=2; B+=0.05) {
            for (; C>=-0.5; C-=0.1) {
                ArrayList<Mthread> threadList = new ArrayList<>();
                for (int i=0; i<Nt; i++) {
                    threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
                }
                for (int i=0; i<Nt; i++) {
                    threadList.get(i).start();
                }
        
                try {
                    Mthread.finished.acquire();
                    // System.out.println("B="+B+" C="+C+" <cp>=" + threadList.get(0).globalMeanC+" diff:"+Math.abs(threadList.get(Nt-1).globalMeanC-1));
                    if (Math.abs(threadList.get(Nt-1).globalMeanC-1)<=5E-4) {
                        System.out.println("Valid Value Found");
                        System.out.println("B="+B+" C="+C+" <cp>=" + threadList.get(0).globalMeanC);
                        threadList.clear();
                        Mthread.globalC.clear();
                        Mthread.globalM.clear();
                        break outerloop;
                    }
                }catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                threadList.clear();
                Mthread.globalC.clear();
                Mthread.globalM.clear();
            }
            C=-0.3;        // reset C after inner loop
        }
        double[] result = {B,C};
        return result;
    }

    public static void findMin(double B, double C) {
        double T=0.01;
        int N = 100;
        int Nt = 1000;
        // use small Nm and Nf to speed up performace
        int Nm = 50;        
        int Nf = 8;
        System.out.println("Exihibiting Vally at T>0 ...");
        for (; T<5; T+=0.2) {
            ArrayList<Mthread> threadList = new ArrayList<>();
            for (int i=0; i<Nt; i++) {
                threadList.add(new Mthread(N, B, C, T, Nm, Nt, Nf, i));
            }
            for (int i=0; i<Nt; i++) {
                threadList.get(i).start();
            }
    
            try {
                Mthread.finished.acquire();
                System.out.println("B="+B + " C="+ C +" T="+T + " <cp>="+threadList.get(0).globalMeanC);
            }catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            threadList.clear();
            Mthread.globalC.clear();
            Mthread.globalM.clear();
        }
    }

}