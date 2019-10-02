package com.larry.floaty.utils;

/**
 * Created by Yanik and Martin on 02/06/2016.
 * Singly Linked List Class
 */

public class SLList<A> {
    public A head;
    public SLList<A> tail;

    public SLList(A head, SLList<A> tail){
        this.head = head;
        this.tail = tail;
    }
}
