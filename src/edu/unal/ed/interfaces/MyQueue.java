package edu.unal.ed.interfaces;

public interface MyQueue<T> {

    void enqueue(T x);

    T dequeue();

    T front();

    boolean isEmpty();

    int size();

    void delete(int n);
}