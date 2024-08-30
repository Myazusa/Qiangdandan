package github.myazusa.collection;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

public class FixedSizeQueue<T> implements Iterable<T> {
    private final int maxSize;
    private final Queue<T> queue;

    /**
     * 初始化函数，如果
     * @param size 传入长度上限
     */
    public FixedSizeQueue(int size) {
        if (size>0){
            this.maxSize = size;
            this.queue = new ArrayDeque<>(size);
        }else {
            throw new RuntimeException("FixedSizeQueue invalid initialization parameter.");
        }
    }
    public FixedSizeQueue(Set<T> set) {
        if (set.size()<=5){
            this.maxSize = set.size();
            this.queue = new ArrayDeque<>(set);
        }else {
            throw new RuntimeException("Set‘s size out of length.");
        }
    }

    public void add(T element) {
        // 如果队列已满，移除最旧的元素
        if (queue.size() == maxSize) {
            queue.poll();  // 移除队列头部
        }
        queue.offer(element);  // 添加新元素到队列尾部
    }

    public Queue<T> getElements() {
        return queue;
    }
    public int getSize(){
        return maxSize;
    }

    public Set<String> convertToStringSet(){
        HashSet<T> hashSet = new HashSet<>(this.queue);
        HashSet<String> stringHashSet = new HashSet<>();
        for (T item: hashSet) {
            stringHashSet.add(item.toString());
        }
        return stringHashSet;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new FixedSizeQueueIterator();
    }
    private class FixedSizeQueueIterator implements Iterator<T>{
        private final Iterator<T> iterator = queue.iterator();
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return iterator.next();
        }
    }
}
