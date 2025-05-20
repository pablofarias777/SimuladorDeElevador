package com.elevator.model;

public class WaitingQueue {
    private static class Node {
        Person person;
        Node next;

        Node(Person person) {
            this.person = person;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public WaitingQueue() {
        head = null;
        tail = null;
        size = 0;
    }

    public void enqueue(Person person) {
        Node newNode = new Node(person);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public Person dequeue() {
        if (isEmpty()) {
            return null;
        }
        Person person = head.person;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return person;
    }

    public Person peek() {
        if (isEmpty()) {
            return null;
        }
        return head.person;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
} 