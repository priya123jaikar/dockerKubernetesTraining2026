public class Demo {
    public static void main(String[] args) {
        System.out.println("Java Demo Program");
        System.out.println("=================");

        int a = 10;
        int b = 20;
        int sum = add(a, b);

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("Sum = " + sum);

        Person person = new Person("Alice", 30);
        System.out.println(person.getGreeting());
    }

    private static int add(int x, int y) {
        return x + y;
    }

    static class Person {
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String getGreeting() {
            return "Hello, my name is " + name + " and I am " + age + " years old.";
        }
    }
}
