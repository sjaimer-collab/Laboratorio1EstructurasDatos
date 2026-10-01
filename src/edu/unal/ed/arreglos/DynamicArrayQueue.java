package edu.unal.ed.arreglos;

import edu.unal.ed.interfaces.MyQueue;

public class DynamicArrayQueue<T> implements MyQueue<T> {
    private T[] array;
    private int front;
    private int size;

    @SuppressWarnings("unchecked")
    public DynamicArrayQueue(int initialCapacity) {
        array = (T[]) new Object[initialCapacity];
        front = 0;
        size = 0;
    }

    public DynamicArrayQueue() {
        this(10);
    }

    // Complejidad amortizada: O(1)
    private void resize() {
        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) new Object[array.length * 2];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[(front + i) % array.length];
        }
        array = newArray;
        front = 0;
    }

    @Override
    public void enqueue(T x) {
        if (size == array.length) {
            resize();
        }
        int rear = (front + size) % array.length;
        array[rear] = x;
        size++;
    }

    @Override
    public T dequeue() {
        if (isEmpty()) return null;
        T data = array[front];
        array[front] = null;
        front = (front + 1) % array.length;
        size--;
        return data;
    }

    @Override
    public T front() {
        if (isEmpty()) return null;
        return array[front];
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void delete(T n) {
        for (int i = 0; i < size; i++) {
            int index = (front + i) % array.length;
            if (array[index].equals(n)) {
                for (int j = i; j < size - 1; j++) {
                    int current = (front + j) % array.length;
                    int next = (front + j + 1) % array.length;
                    array[current] = array[next];
                }
                int last = (front + size - 1) % array.length;
                array[last] = null;
                size--;
                return;
            }
        }
    }
}