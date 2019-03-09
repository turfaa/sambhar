package io.sentry.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class CircularFifoQueue<E> extends AbstractCollection<E> implements Queue<E>, Serializable {
    private static final long serialVersionUID = -8423413834657610406L;
    private transient E[] elements;
    private transient int end;
    private transient boolean full;
    private final int maxElements;
    private transient int start;

    public boolean isFull() {
        return false;
    }

    public CircularFifoQueue() {
        this(32);
    }

    public CircularFifoQueue(int i) {
        this.start = 0;
        this.end = 0;
        this.full = false;
        if (i > 0) {
            this.elements = new Object[i];
            this.maxElements = this.elements.length;
            return;
        }
        throw new IllegalArgumentException("The size must be greater than 0");
    }

    public CircularFifoQueue(Collection<? extends E> collection) {
        this(collection.size());
        addAll(collection);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(size());
        Iterator it = iterator();
        while (it.hasNext()) {
            objectOutputStream.writeObject(it.next());
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.elements = new Object[this.maxElements];
        int readInt = objectInputStream.readInt();
        for (int i = 0; i < readInt; i++) {
            this.elements[i] = objectInputStream.readObject();
        }
        this.start = 0;
        this.full = readInt == this.maxElements;
        if (this.full) {
            this.end = 0;
        } else {
            this.end = readInt;
        }
    }

    public int size() {
        if (this.end < this.start) {
            return (this.maxElements - this.start) + this.end;
        }
        if (this.end == this.start) {
            return this.full ? this.maxElements : 0;
        } else {
            return this.end - this.start;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isAtFullCapacity() {
        return size() == this.maxElements;
    }

    public int maxSize() {
        return this.maxElements;
    }

    public void clear() {
        this.full = false;
        this.start = 0;
        this.end = 0;
        Arrays.fill(this.elements, null);
    }

    public boolean add(E e) {
        if (e != null) {
            if (isAtFullCapacity()) {
                remove();
            }
            Object[] objArr = this.elements;
            int i = this.end;
            this.end = i + 1;
            objArr[i] = e;
            if (this.end >= this.maxElements) {
                this.end = 0;
            }
            if (this.end == this.start) {
                this.full = true;
            }
            return true;
        }
        throw new NullPointerException("Attempted to add null object to queue");
    }

    public E get(int i) {
        int size = size();
        if (i < 0 || i >= size) {
            throw new NoSuchElementException(String.format("The specified index (%1$d) is outside the available range [0, %2$d)", new Object[]{Integer.valueOf(i), Integer.valueOf(size)}));
        }
        return this.elements[(this.start + i) % this.maxElements];
    }

    public boolean offer(E e) {
        return add(e);
    }

    public E poll() {
        if (isEmpty()) {
            return null;
        }
        return remove();
    }

    public E element() {
        if (!isEmpty()) {
            return peek();
        }
        throw new NoSuchElementException("queue is empty");
    }

    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return this.elements[this.start];
    }

    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("queue is empty");
        }
        E e = this.elements[this.start];
        if (e != null) {
            Object[] objArr = this.elements;
            int i = this.start;
            this.start = i + 1;
            objArr[i] = null;
            if (this.start >= this.maxElements) {
                this.start = 0;
            }
            this.full = false;
        }
        return e;
    }

    private int increment(int i) {
        i++;
        return i >= this.maxElements ? 0 : i;
    }

    private int decrement(int i) {
        i--;
        return i < 0 ? this.maxElements - 1 : i;
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int index = CircularFifoQueue.this.start;
            private boolean isFirst = CircularFifoQueue.this.full;
            private int lastReturnedIndex = -1;

            public boolean hasNext() {
                return this.isFirst || this.index != CircularFifoQueue.this.end;
            }

            public E next() {
                if (hasNext()) {
                    this.isFirst = false;
                    this.lastReturnedIndex = this.index;
                    this.index = CircularFifoQueue.this.increment(this.index);
                    return CircularFifoQueue.this.elements[this.lastReturnedIndex];
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                if (this.lastReturnedIndex == -1) {
                    throw new IllegalStateException();
                } else if (this.lastReturnedIndex == CircularFifoQueue.this.start) {
                    CircularFifoQueue.this.remove();
                    this.lastReturnedIndex = -1;
                } else {
                    int i = this.lastReturnedIndex + 1;
                    if (CircularFifoQueue.this.start >= this.lastReturnedIndex || i >= CircularFifoQueue.this.end) {
                        while (i != CircularFifoQueue.this.end) {
                            if (i >= CircularFifoQueue.this.maxElements) {
                                CircularFifoQueue.this.elements[i - 1] = CircularFifoQueue.this.elements[0];
                                i = 0;
                            } else {
                                CircularFifoQueue.this.elements[CircularFifoQueue.this.decrement(i)] = CircularFifoQueue.this.elements[i];
                                i = CircularFifoQueue.this.increment(i);
                            }
                        }
                    } else {
                        System.arraycopy(CircularFifoQueue.this.elements, i, CircularFifoQueue.this.elements, this.lastReturnedIndex, CircularFifoQueue.this.end - i);
                    }
                    this.lastReturnedIndex = -1;
                    CircularFifoQueue.this.end = CircularFifoQueue.this.decrement(CircularFifoQueue.this.end);
                    CircularFifoQueue.this.elements[CircularFifoQueue.this.end] = null;
                    CircularFifoQueue.this.full = false;
                    this.index = CircularFifoQueue.this.decrement(this.index);
                }
            }
        };
    }
}
