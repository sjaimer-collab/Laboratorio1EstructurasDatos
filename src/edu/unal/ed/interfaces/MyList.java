package edu.unal.ed.interfaces;

public interface MyList<T> {

    void pushFront(T key);

    void pushBack(T key);

    T popFront();

    T popBack();

    T find(T key);

    void erase(T key);

    void addBefore(T nodeKey, T key);

    void addAfter(T nodeKey, T key);

    boolean isEmpty();
}