import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6};
        calculateParallelSum(array);
    }

    public static void calculateParallelSum(int[] array) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<Integer> partialSums = calculatePartialSums(array, executorService);

            while (partialSums.size() > 1) {
                partialSums = calculatePartialSums(partialSums.stream().mapToInt(Integer::intValue).toArray(), executorService);
                System.out.println("Проміжний результат обчислень: " + partialSums);
            }

            System.out.println("Фінальний результат: " + partialSums.get(0));
        } catch (InterruptedException e) {
            System.err.println("Помилка при очікуванні завершення потоків: " + e.getMessage());
        }
    }

    private static List<Integer> calculatePartialSums(int[] array, ExecutorService executorService) throws InterruptedException {
        int newIterationArrayLength = (array.length % 2 == 0) ? array.length / 2 : (array.length / 2) + 1;
        List<Integer> partialSums = new CopyOnWriteArrayList<>();

        CountDownLatch latch = new CountDownLatch(newIterationArrayLength);
        for (int i = 0; i < newIterationArrayLength; i++) {
            int[] finalArray = array;
            int finalI = i;

            executorService.execute(() -> {
                new ArraySumTask(finalArray, newIterationArrayLength, finalI, partialSums, latch).run();
                latch.countDown();
            });
        }

        latch.await();
        return partialSums;
    }
}

class ArraySumTask implements Runnable {
    private final int[] actualArray;
    private final int newIterationArrayLength;
    private final int index;
    private final List<Integer> partialSums;
    private final CountDownLatch latch;

    ArraySumTask(int[] actualArray, int newIterationArrayLength, int index, List<Integer> partialSums, CountDownLatch latch) {
        this.actualArray = actualArray;
        this.newIterationArrayLength = newIterationArrayLength;
        this.index = index;
        this.partialSums = partialSums;
        this.latch = latch;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started work");
        final int index2 = actualArray.length - 1 - index;

        int value = actualArray[index] + ((index2 >= newIterationArrayLength) ? actualArray[index2] : 0);
        partialSums.add(value);
        System.out.println(index + " - finalIndex  " + partialSums);
        latch.countDown();
    }
}
