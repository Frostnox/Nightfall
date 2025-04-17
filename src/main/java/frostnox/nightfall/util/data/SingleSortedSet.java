package frostnox.nightfall.util.data;

import java.util.*;

public class SingleSortedSet<E> extends AbstractSet<E> implements SortedSet<E> {
    private E element;

    public SingleSortedSet(E element) {
        this.element = Objects.requireNonNull(element);
    }

    @Override
    public boolean add(E e) {
        element = e;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.singleton(element).iterator();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public E first() {
        return element;
    }

    @Override
    public E last() {
        return element;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return !element.equals(toElement) ? this : Collections.emptySortedSet();
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return element.equals(fromElement) ? this : Collections.emptySortedSet();
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return (element.equals(fromElement) && !element.equals(toElement)) ? this : Collections.emptySortedSet();
    }
}
