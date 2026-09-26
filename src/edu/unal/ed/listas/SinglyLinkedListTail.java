package edu.unal.ed.listas;

import edu.unal.ed.interfaces.MyList;
import edu.unal.ed.nodos.Node;

public class SinglyLinkedListTail<T> implements MyList<T> {
    private Node<T> head;
    private Node<T> tail;

    public SinglyLinkedListTail() {
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
        Node<T> newNode = new Node<>(key);
        newNode.next = head;
        head = newNode;
        if (tail == null) {
            tail = head;
        }
    }

    // Complejidad: O(1)
    @Override
    public void pushBack(T key) {
        Node<T> newNode = new Node<>(key);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    // Complejidad: O(1)
    @Override
    public T popFront() {
        if (isEmpty()) return null;
        T data = head.data;
        head = head.next;
        if (head == null) { // Si la lista quedó vacía, tail también debe ser null
            tail = null;
        }
        return data;
    }

    // Complejidad: O(n)
    @Override
    public T popBack() {
        if (isEmpty()) return null;
        if (head == tail) { // Solo hay un elemento
            T data = head.data;
            head = tail = null;
            return data;
        }
        Node<T> current = head;
        while (current.next != tail) {
            current = current.next;
        }
        T data = tail.data;
        tail = current;
        tail.next = null;
        return data;
    }

    // Complejidad: O(n)
    @Override
    public T find(T key) {
        Node<T> current = head;
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
        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(key)) {
            current = current.next;
        }
        if (current.next != null) {
            if (current.next == tail) {
                tail = current;
            }
            current.next = current.next.next;
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
        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current.next != null) {
            Node<T> newNode = new Node<>(key);
            newNode.next = current.next;
            current.next = newNode;
        }
    }

    // Complejidad: O(n)
    @Override
    public void addAfter(T nodeKey, T key) {
        Node<T> current = head;
        while (current != null && !current.data.equals(nodeKey)) {
            current = current.next;
        }
        if (current != null) {
            Node<T> newNode = new Node<>(key);
            newNode.next = current.next;
            current.next = newNode;
            if (current == tail) {
                tail = newNode;
            }
        }
    }
}