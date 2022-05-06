package itmo.vkr;

import executor.MavenExecutor;

public class Main {
    public static void main(String[] args) {
        Collector collector = new Collector();

        MavenExecutor.execute("mvn.cmd compile"); // генерируем исходники

        while (!collector.isGenerated()){
            try {
                System.out.println("NO");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DONE!");

        collector.collectSources();


    }
}
