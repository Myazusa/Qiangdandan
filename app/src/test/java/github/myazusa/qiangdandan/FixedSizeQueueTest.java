package github.myazusa.qiangdandan;

import org.junit.Test;

import github.myazusa.collection.FixedSizeQueue;

public class FixedSizeQueueTest {
    @Test
    public void fixedSizeQueueIteratorTest(){
        FixedSizeQueue<Integer> fixedSizeQueue = new FixedSizeQueue<>(6);
        fixedSizeQueue.add(1);
        fixedSizeQueue.add(2);
        fixedSizeQueue.add(3);
        fixedSizeQueue.add(4);
        fixedSizeQueue.add(5);
        fixedSizeQueue.add(6);
        fixedSizeQueue.add(7);
        fixedSizeQueue.add(8);
        fixedSizeQueue.add(9);
        fixedSizeQueue.add(10);
        for (Integer i:fixedSizeQueue) {
            System.out.println(i);
        }
        System.out.println(fixedSizeQueue.getSize());
    }
}
