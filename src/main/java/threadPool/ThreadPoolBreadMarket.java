package threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolBreadMarket {
    public static void main(String[] args) {
        Operation operation = new Operation();
        Producer producer = new Producer(operation);
        Seller seller = new Seller(operation);
        Consumer consumer = new Consumer(operation);

        ExecutorService es = Executors.newFixedThreadPool(3);
        es.submit(producer);
        es.submit(seller);
        es.submit(consumer);

        es.shutdown();
    }
}

class Operation {

    private final int marketPlan = 15;              // The market-plan of bread production

    private final int portionOfProduce = 5;         // Portion of the bread production
    private int prducerStorage = 0;                 // Storage of the Producer
    private int sellerStorage = 0;                  // Storage of the Seller
    private boolean isProduce = true;
    private boolean isSale = false;
    private boolean isConsume = false;

    public int getMarketPlan() {
        return marketPlan;
    }

    public int getPortionOfProduce() {
        return portionOfProduce;
    }

    public synchronized void getBread() {
        while (!isSale) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (sellerStorage == 0 && prducerStorage == 0) {
            isSale = false;
            isConsume = false;       // Seller allow work the Consumer
            isProduce = true;       // Seller allow work the Producer
            notifyAll();
        } else {
            prducerStorage--;
            sellerStorage++;
            System.out.println("\tSELLER received a bread to sale");
            System.out.println("\tSELLER: Bread quantity of SELLER is: " + sellerStorage);
            System.out.println("\tSELLER: Bread quantity of PRODUCER is: " + prducerStorage);
            if (sellerStorage == portionOfProduce) {
                isSale = false;
                isConsume = true;       // Seller allow work the Consumer
                isProduce = false;       // Seller allow work the Producer
                notifyAll();
            }
        }
    }

    public synchronized void putBread() {
        while (!isProduce) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        prducerStorage++;
        System.out.println("PRODUCER has produced a bread");
        System.out.println("PRODUCER:Bread quantity of PRODUCER is: " + prducerStorage);
        if (prducerStorage == portionOfProduce) {
            isProduce = false;
            isSale = true;
            isConsume = false;
            notifyAll();
        }
    }

    public synchronized void buyBread() {
        while (!isConsume) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sellerStorage--;
        System.out.println("\t\tCONSUMER eats a bread");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("\t\tCONSUMER:Bread quantity of SELLER is: " + sellerStorage);
        if (sellerStorage == 0) {
            isConsume = false;
            isSale = true;
            isProduce = false;       //Consumer allow work the Producer
            notifyAll();
        }
    }
}

class Producer implements Runnable {
    Operation operation;

    public Producer(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        for (int i = 0; i < operation.getMarketPlan(); i++) {
            System.out.println("PRODUCER call = " + i);
            operation.putBread();
        }
    }
}

class Seller implements Runnable {
    Operation operation;

    public Seller(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        int nCalls = operation.getMarketPlan() * (operation.getPortionOfProduce()+ 1)/operation.getPortionOfProduce()  - 1;
        for (int i = 0; i < nCalls; i++) {
            System.out.println("\tSELLER call = " + i);
            operation.getBread();
        }
    }
}

class Consumer implements Runnable {
    Operation operation;

    public Consumer(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        for (int i = 0; i < operation.getMarketPlan(); i++) {
            System.out.println("\t\tCONSUMER call = " + i);
            operation.buyBread();
        }
    }
}
