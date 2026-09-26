package edu.unal.ed.listas;

import edu.unal.ed.interfaces.MyList;
import edu.unal.ed.nodos.DoubleNode;

public class DoublyLinkedList<T> implements MyList<T> {
    private DoubleNode<T> head;

    public DoublyLinkedList() {
        this.head = null;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public void pushFront(T key) {
        DoubleNode<T> newNode = new DoubleNode<>(key);
        if (!isEmpty()) {
            head.prev = newNode;
        }
        newNode.next = head;
        head = newNode;
    }

    @Override
    public void pushBack(T key) {
        DoubleNode<T> newNode = new DoubleNode<>(key);
        if (isEmpty()) {
            head = newNode;
            return;
        }
        DoubleNode<T> current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
        newNode.prev = current;
    }

    @Override
    public T popFront() {
        if (isEmpty()) return null;
        T data = head.data;
        head = head.next;
        if (head != null) {
            head.prev = null;
        }
        return data;
    }

    @Override
    public T popBack() {
        if (isEmpty()) return null;
        if (head.next == null) {
            T data = head.data;
            head = null;
            return data;
        }
        DoubleNode<T> current = head;
        while (current.next != null) {
            current = current.next;
        }
        T data = current.data;
        current.prev.next = null;
        return data;
    }

    @Override
    public T find(T key) {
        DoubleNode<T> current = head;
        while (current != null) {
            if (current.data.equals(key)) return current.data;
            current = current.next;
        }
        return null;
    }

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
            if (current.next != null) {
                current.next.prev = current.prev;
            }
            current.prev.next = current.next;
        }
    }

    @Override
    public void addBefore(T nodeKey, T key) {
        DoubleNode<T> current = head;
        while (current != null && !current.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current != null) {
            if (current == head) {
                pushFront(key);
            } else {
                DoubleNode<T> newNode = new DoubleNode<>(key);
                newNode.next = current;
                newNode.prev = current.prev;
                current.prev.next = newNode;
                current.prev = newNode;
            }
        }
    }

    @Override
    public void addAfter(T nodeKey, T key) {
        DoubleNode<T> current = head;
        while (current != null && !current.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current != null) {
            DoubleNode<T> newNode = new DoubleNode<>(key);
            newNode.next = current.next;
            newNode.prev = current;
            if (current.next != null) {
                current.next.prev = newNode;
            }
            current.next = newNode;
        }
    }
}