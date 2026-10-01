package edu.unal.ed.arreglos;

import edu.unal.ed.interfaces.MyStack;

public class DynamicArrayStack<T> implements MyStack<T> {
    private T[] array;
    private int top;

    @SuppressWarnings("unchecked")
    public DynamicArrayStack(int initialCapacity) {
        array = (T[]) new Object[initialCapacity];
        top = 0;
    }

    public DynamicArrayStack() {
        this(10);
    }

    private void resize() {
        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) new Object[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    @Override
    public void push(T x) {
        if (top == array.length) {
            resize();
        }
        array[top] = x;
        top++;
    }

    @Override
    public T pop() {
        if (isEmpty()) return null;
        top--;
        T data = array[top];
        array[top] = null;
        return data;
    }

    @Override
    public T peek() {
        if (isEmpty()) return null;
        return array[top - 1];
    }

    @Override
    public boolean isEmpty() {
        return top == 0;
    }

    @Override
    public int size() {
        return top;
    }

    @Override
    public void delete(T n) {
        for (int i = top - 1; i >= 0; i--) {
            if (array[i].equals(n)) {

                for (int j = i; j < top - 1; j++) {
                    array[j] = array[j + 1];
                }
                array[top - 1] = null;
                top--;
                return;
            }
        }
    }
}