package github.myazusa.collection;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class FixedSizeQueue<T> {
    private final int maxSize;
    private final Queue<T> queue;

    public FixedSizeQueue(int size) {
        this.maxSize = size;
        this.queue = new ArrayDeque<>(size);
    }

    public void add(T element) {
        // 如果队列已满，移除最旧的元素
        if (queue.size() == maxSize) {
            queue.poll();  // 移除队列头部（最旧的元素）
        }
        queue.offer(element);  // 添加新元素到队列尾部
    }

    public Queue<T> getElements() {
        return queue;
    }
    // TODO: 实现迭代器

    public Set<String> convertToStringSet(){
        HashSet<T> hashSet = new HashSet<>(this.queue);
        HashSet<String> stringHashSet = new HashSet<>();
        for (T item: hashSet) {
            stringHashSet.add(item.toString());
        }
        return stringHashSet;
    }
}
