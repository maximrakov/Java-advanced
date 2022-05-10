import java.util.*;

public class ArraySet<E extends Comparable<E> > extends AbstractSet<E> implements NavigableSet<E> {

    private Comparator<? super E> comparator;
    private ReversibleList<? extends E> list;

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        final NavigableSet<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.list = new ReversibleList<>(set);
        this.comparator = comparator;
    }

    public ArraySet(List<? extends E> list) {
        this(list, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(new ArrayList<>(), comparator);
    }

    @Override
    public E lower(E e) {
        E result = list.get(upperBound(e, false));
        if(e.compareTo(result) < 0) {
            return null;
        }
        return result;
    }

    @Override
    public E floor(E e) {
        E result = list.get(upperBound(e, true));
        if(e.compareTo(result) < 0) {
            return null;
        }
        return result;
    }

    @Override
    public E ceiling(E e) {
        E result = list.get(lowerBound(e, true));
        if(e.compareTo(result) > 0) {
            return null;
        }
        return result;
    }

    @Override
    public E higher(E e) {
        E result = list.get(lowerBound(e, false));
        if(e.compareTo(result) > 0) {
            return null;
        }
        return result;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) list.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversibleList<E>(list, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int indexFrom = lowerBound(fromElement, fromInclusive);
        int indexTo = upperBound(toElement, toInclusive);
        return new ArraySet<>(this.list.subList(indexFrom, indexTo + 1));
    }

    private int upperBound(E toElement, boolean toInclusive) {
        int index = Collections.binarySearch(list, toElement, comparator);
        if(index < 0) {
            index = -index - 1;
        }
        if (!toInclusive) {
            index--;
        }
        return Integer.min(size() - 1, index);
    }

    private int lowerBound(E fromElement, boolean fromInclusive) {
        int index = Collections.binarySearch(list, fromElement, comparator);
        if(index < 0) {
            index = -index - 1;
        }
        if (!fromInclusive) {
            index++;
        }
        return Integer.min(size() - 1, index);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int indexTo = upperBound(toElement, inclusive);
        return new ArraySet<>(list.subList(0, indexTo), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int indexFrom = lowerBound(fromElement, inclusive);
        return new ArraySet<>(list.subList(indexFrom, list.size() - 1));
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return this.subSet(fromElement,true, toElement,false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public E first() {
        return list.get(0);
    }

    @Override
    public E last() {
        return list.get(list.size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    public static class ReversibleList<E> extends AbstractList<E> implements RandomAccess {

        private final List<? extends E> list;
        private boolean isReversed = false;

        public ReversibleList(Collection<? extends E> list) {
            this.list = List.copyOf(list);
            this.isReversed = false;
        }

        public ReversibleList(final ReversibleList<? extends E> list, boolean isReversed) {
            this.list = list;
            this.isReversed = list.isReversed ^ isReversed;
        }

        public int getReverseIndex(int index) {
            return size() - 1 - index;
        }

        @Override
        public ReversibleList<E> subList(int fromIndex, int toIndex) {
            return new ReversibleList<>(list.subList(fromIndex, toIndex));
        }

        @Override
        public E get(int index) {
            if(isReversed) {
                return list.get(getReverseIndex(index));
            } else {
                return list.get(index);
            }
        }

        @Override
        public int size() {
            return list.size();
        }
    }
}