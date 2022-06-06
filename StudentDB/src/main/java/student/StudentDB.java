package student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDB implements GroupQuery {

    private <T> List<T> studentMapper(Collection<Student> students, Function<Student, T> mapping) {
        return students
                .stream()
                .map(mapping)
                .collect(Collectors.toList());
    }

    private <T extends Comparable<? super T> > List<Student> studentSort(Collection<Student> students, Function<Student, T> key) {
        return students
                .stream()
                .sorted(Comparator.comparing(key))
                .collect(Collectors.toList());
    }

    private List<Student> findStudentsBy(Collection<Student> students, Predicate<Student> predicate) {
        return students
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Group> getGroupsBy(Collection<Student> students, Function<List<Student>, List<Student>> mapper) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .map(e -> new Group(e.getKey(), mapper.apply(e.getValue())))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());

    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students,
                s -> s.stream()
                .sorted(Comparator.comparing(Student::getFirstName))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students,
                s -> s.stream()
                        .sorted(Comparator.comparing(Student::getId))
                        .collect(Collectors.toList()));
    }

    private GroupName getLargestGroupFromStudentsBy(Collection<Student> students,
                                                   Function<List<Student>, Integer> mapper,
                                                   Comparator<GroupName> keyComparator
                                                   ) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mapper.apply(e.getValue())))
                .entrySet().stream()
                .max(Map.Entry.
                        <GroupName, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(keyComparator))).
                map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupFromStudentsBy(students, List::size, Comparator.naturalOrder());
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupFromStudentsBy(students, s -> getDistinctFirstNames(s).size(), Comparator.reverseOrder());;
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return studentMapper(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return studentMapper(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return studentMapper(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return studentMapper(students, student -> student.getFirstName() + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream().collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().
                max(Comparator.comparingInt(Student::getId)).
                map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return studentSort(students, Student::getId);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return studentSort(students, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortStudentsByName(findStudentsBy(students, (student -> student.getFirstName().equals(name))));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return sortStudentsByName(findStudentsBy(students, student -> student.getLastName().equals(name)));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return sortStudentsByName(findStudentsBy(students, student -> student.getGroup().equals(group)));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }
}
