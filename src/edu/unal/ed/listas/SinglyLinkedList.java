package edu.unal.ed.listas;

import edu.unal.ed.interfaces.MyList;
import edu.unal.ed.nodos.Node;

public class SinglyLinkedList<T> implements MyList<T> {
    private Node<T> head;

    public SinglyLinkedList() {
        this.head = null;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public void pushFront(T key) {
        Node<T> newNode = new Node<>(key);
        newNode.next = head;
        head = newNode;
    }

    @Override
    public void pushBack(T key) {
        Node<T> newNode = new Node<>(key);
        if (isEmpty()) {
            head = newNode;
            return;
        }
        Node<T> current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
    }

    @Override
    public T popFront() {
        if (isEmpty()) return null; // O podrías lanzar una excepción
        T data = head.data;
        head = head.next;
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
        Node<T> current = head;
        while (current.next.next != null) {
            current = current.next;
        }
        T data = current.next.data;
        current.next = null;
        return data;
    }

    @Override
    public T find(T key) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(key)) {
                return current.data;
            }
            current = current.next;
        }
        return null; // No lo encontró
    }

    @Override
    public void erase(T key) {
        if (isEmpty()) return;
        if (head.data.equals(key)) {
            head = head.next;
            return;
        }
        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(key)) {
            current = current.next;
        }
        if (current.next != null) {
            current.next = current.next.next;
        }
    }

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
        }
    }
}