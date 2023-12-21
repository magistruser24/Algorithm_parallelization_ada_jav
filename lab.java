import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelArraySumCalculator {

    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6};
        calculateParallelArraySum(array);
    }

    public static void calculateParallelArraySum(int[] array) {
        List<int[]> iterations = new ArrayList<>();
        iterations.add(array.clone());

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long startTime = System.nanoTime();

        while (array.length > 1) {
            array = calculateNextArray(array, executorService, iterations);
        }

        executorService.shutdown();

        long endTime = System.nanoTime();
        long executionTimeMs = (endTime - startTime) / 1_000_000; // Convert nanoseconds to milliseconds

        // Print intermediate arrays
        printIterations(iterations);

        System.out.println("Final result: " + array[0]);
        System.out.println("Execution time: " + executionTimeMs + " ms");
    }

    private static int[] calculateNextArray(int[] array, ExecutorService executorService, List<int[]> iterations) {
        int newArrayLength = (array.length % 2 == 0) ? array.length / 2 : (array.length / 2) + 1;
        int[] newArray = new int[newArrayLength];

        int[] results = new int[newArrayLength];
        CountDownLatch latch = new CountDownLatch(newArrayLength);

        for (int i = 0; i < newArrayLength; i++) {
            final int index1 = i;
            final int index2 = array.length - 1 - i;

            int value = array[index1] + ((index2 >= newArrayLength) ? array[index2] : 0);

            int finalIndex = i; // Add this line to make the variable effectively final

            executorService.submit(() -> {
                results[finalIndex] = value;
                latch.countDown();
            });
        }

        awaitLatch(latch);

        System.arraycopy(results, 0, newArray, 0, newArrayLength);

        iterations.add(newArray.clone());

        return newArray;
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printArray(int[] array) {
        for (int element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    private static void printIterations(List<int[]> iterations) {
        System.out.println("Intermediate arrays after each iteration:");
        for (int i = 1; i < iterations.size(); i++) {
            System.out.print("Iteration " + i + ": ");
            printArray(iterations.get(i));
        }
    }
}
