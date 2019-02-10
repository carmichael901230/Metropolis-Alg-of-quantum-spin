class Main {
    public static void main(String[] args) {
        double B = 1.0, C = -1.0, T = 0.1;
        int N = 100;
        Configuration test = new Configuration(N, B, C, T);
        
        for (int i=0; i<100; i++) {
            test.updateCurrentSigma();
        }
        for (int e:test.sigma) {
            System.out.print(e);
        }
        System.out.println();

    }
}