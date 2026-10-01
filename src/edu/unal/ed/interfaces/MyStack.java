package edu.unal.ed.interfaces;

public interface MyStack<T> {

    void push(T x);

    T pop();

    T peek();

    boolean isEmpty();

    int size();

    void delete(T n);
}