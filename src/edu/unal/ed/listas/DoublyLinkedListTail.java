package edu.unal.ed.listas;

import edu.unal.ed.interfaces.MyList;
import edu.unal.ed.nodos.DoubleNode;

public class DoublyLinkedListTail<T> implements MyList<T> {
    private DoubleNode<T> head;
    private DoubleNode<T> tail; // Referencia al final

    public DoublyLinkedListTail() {
        this.head = null;
        this.tail = null;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    // Complejidad: O(1)
    @Override
    public void pushFront(T key) {
        DoubleNode<T> newNode = new DoubleNode<>(key);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }

    // Complejidad: O(1)
    @Override
    public void pushBack(T key) {
        DoubleNode<T> newNode = new DoubleNode<>(key);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    // Complejidad: O(1)
    @Override
    public T popFront() {
        if (isEmpty()) return null;
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null; // La lista quedó vacía
        } else {
            head.prev = null;
        }
        return data;
    }

    // Complejidad: O(1)
    @Override
    public T popBack() {
        if (isEmpty()) return null;
        T data = tail.data;
        tail = tail.prev;
        if (tail == null) {
            head = null; // La lista quedó vacía
        } else {
            tail.next = null;
        }
        return data;
    }

    // Complejidad: O(n)
    @Override
    public T find(T key) {
        DoubleNode<T> current = head;
        while (current != null) {
            if (current.data.equals(key)) return current.data;
            current = current.next;
        }
        return null;
    }

    // Complejidad: O(n)
    @Override
    public void erase(T key) {
        if (isEmpty()) return;
        if (head.data.equals(key)) {
            popFront();
            return;
        }
        DoubleNode<T> current = head;
        while (current != null && !current.data.equals(key)) {
            current = current.next;
        }
        if (current != null) {
            if (current == tail) {
                popBack();
            } else {
                current.prev.next = current.next;
                current.next.prev = current.prev;
            }
        }
    }

    // Complejidad: O(n)
    @Override
    public void addBefore(T nodeKey, T key) {
        if (isEmpty()) return;
        if (head.data.equals(nodeKey)) {
            pushFront(key);
            return;
        }
        DoubleNode<T> current = head;
        while (current != null && !current.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current != null) {
            DoubleNode<T> newNode = new DoubleNode<>(key);
            newNode.next = current;
            newNode.prev = current.prev;
            current.prev.next = newNode;
            current.prev = newNode;
        }
    }

    // Complejidad: O(n)
    @Override
    public void addAfter(T nodeKey, T key) {
        DoubleNode<T> current = head;
        while (current != null && !current.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current != null) {
            if (current == tail) {
                pushBack(key);
            } else {
                DoubleNode<T> newNode = new DoubleNode<>(key);
                newNode.next = current.next;
                newNode.prev = current;
                current.next.prev = newNode;
                current.next = newNode;
            }
        }
    }
}