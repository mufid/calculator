// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

public class PriorityQueueOfInt {
    private int size;
    private int[] keys, values;

    public PriorityQueueOfInt(int initialCapacity) {
        keys = new int[initialCapacity + 1];
        values = new int[initialCapacity + 1];
        keys[0] = Integer.MAX_VALUE;
    }

    public int extractMaximumValue() { // assumes non-empty
        int max = values[1];
        assign(1, size--);
        heapify(1);
        return max;
    }

    public int getMaximumValue() { return keys[1]; } // assumes non-empty

    public int size() { return size; }

    public void insert(int key, int value) {
        ensureCapacity(++size);
        values[size] = value;
        increase(size, key);
    }

    public void rawInsert(int key, int value) {
        ensureCapacity(++size);
        keys[size] = key;
        values[size] = value;
    }

    public void repairHeap() {
        for (int i = size / 2; i > 0; --i)
            heapify(i);
    }

    public void increaseMaximumKey(int key) {
        increase(1, key);
    }

    private void increase(int pos, int key) {
        // assert key >= array[pos]
        int value = values[pos], i = pos;
        for (; key > keys[i/2]; i /= 2)
            assign(i, i/2);
        keys[i] = key;
        values[i] = value;
    }

    private void heapify(int i) {
        int child;
        int key = keys[i], value = values[i];

        for(; i * 2 <= size; i = child) {
            child = i * 2;
            if (child < size && keys[child + 1] > keys[child])
                ++child;
            if (keys[child] > key)
                assign(i, child);
            else
                break;
        }
        keys[i] = key;
        values[i] = value;
    }

    private void ensureCapacity(int capacity) {
        if (capacity >= keys.length) {
            int[] newKeys = new int[keys.length * 2], newValues = new int[keys.length * 2];
            System.arraycopy(keys,   0, newKeys,   0, size);
            System.arraycopy(values, 0, newValues, 0, size);
            keys = newKeys;
            values = newValues;
        }
    }

    private void assign(int dst, int src) {
        keys[dst] = keys[src];
        values[dst] = values[src];
    }

/*
    import java.util.Arrays;
    import java.util.Random;

    public static void main(String[] args) {
        PriorityQueueOfInt pq = new PriorityQueueOfInt(30);
        int[] items = new int[100];
        Random rng = new Random();
        for (int i = 0; i < 100; ++i) {
            int x = rng.nextInt(1000);
            pq.insert(items[i] = x, x + 55);
        }
        Arrays.sort(items);
        for (int i = 0; i < 100; ++i)
            if (pq.extractMaximumValue() != items[99 - i] + 55)
                System.out.println("Oops");
        System.out.println("Done 1/2");
        
        pq = new PriorityQueueOfInt(10);
        pq.insert(1, 111);
        pq.insert(3, 333);
        pq.insert(2, 222);
        pq.increaseMaximumKey(7);
        pq.insert(4, 444);
        if (pq.extractMaximumValue() != 333)
            System.out.println("Oopsy");
        System.out.println("Done 2/2");
    }
*/
}