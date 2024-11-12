package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList("sin", "cos", "sqrt", "log", "exp", "abs"));
    private static final Map<String, Double> variables = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите выражение:");
        String expression = scanner.nextLine();

        try {
            // Найдем все переменные и запросим их значения
            Set<String> variableNames = findVariables(expression);
            for (String var : variableNames) {
                if (!variables.containsKey(var)) {
                    System.out.print("Введите значение для переменной " + var + ": ");
                    variables.put(var, scanner.nextDouble());
                }
            }

            // Вычисляем выражение
            double result = evaluate(expression);
            System.out.println("Результат: " + result);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    // Функция для нахождения переменных (имена переменных начинаются с буквы)
    private static Set<String> findVariables(String expression) {
        Set<String> variableNames = new HashSet<>();
        Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String var = matcher.group();
            if (!FUNCTIONS.contains(var)) {  // Игнорируем имена функций
                variableNames.add(var);
            }
        }
        return variableNames;
    }

    // Основная функция для вычисления выражения
    private static double evaluate(String expression) throws Exception {
        return evaluateExpression(expression.replaceAll("\\s+", ""));
    }

    // Парсинг и вычисление выражения
    private static double evaluateExpression(String expression) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<String> ops = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // Пропускаем пробелы
            if (Character.isWhitespace(c)) {
                continue;
            }

            // Если это число или переменная
            if (Character.isDigit(c) || Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();

                // Считываем число или переменную
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || Character.isLetter(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                i--;

                String token = sb.toString();
                if (isNumeric(token)) {
                    values.push(Double.parseDouble(token));
                } else if (variables.containsKey(token)) {
                    values.push(variables.get(token));
                } else if (FUNCTIONS.contains(token)) {
                    ops.push(token); // Это функция
                } else {
                    throw new Exception("Неизвестная переменная или функция: " + token);
                }
            }
            // Если это открывающая скобка
            else if (c == '(') {
                ops.push(String.valueOf(c));
            }
            // Если это закрывающая скобка, выполняем вычисления до открывающей скобки
            else if (c == ')') {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop(); // Убираем '('

                // Если перед скобками была функция, применяем её
                if (!ops.isEmpty() && FUNCTIONS.contains(ops.peek())) {
                    values.push(applyFunction(ops.pop(), values.pop()));
                }
            }
            // Если это оператор
            else if (isOperator(c)) {
                // Вычисляем операции с более высоким приоритетом
                while (!ops.isEmpty() && hasPrecedence(String.valueOf(c), ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(String.valueOf(c));
            }
        }

        // Вычисляем оставшиеся операции
        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        // Результат — единственное значение в стеке
        return values.pop();
    }

    // Проверка, является ли строка числом
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Применение оператора
    private static double applyOp(String op, double b, double a) throws Exception {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) throw new Exception("Деление на ноль");
                return a / b;
            case "%":
                if (b == 0) throw new Exception("Деление на ноль");
                return a % b;
            default:
                throw new Exception("Неизвестная операция: " + op);
        }
    }

    // Применение функции
    private static double applyFunction(String function, double value) throws Exception {
        switch (function) {
            case "sin":
                return Math.sin(value);
            case "cos":
                return Math.cos(value);
            case "sqrt":
                if (value < 0) throw new Exception("Невозможно взять корень из отрицательного числа");
                return Math.sqrt(value);
            case "log":
                if (value <= 0) throw new Exception("Логарифм от неположительного числа");
                return Math.log(value);
            case "exp":
                return Math.exp(value);
            case "abs":
                return Math.abs(value);
            default:
                throw new Exception("Неизвестная функция: " + function);
        }
    }

    // Проверка, является ли символ оператором
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    // Проверка приоритетов операторов
    private static boolean hasPrecedence(String op1, String op2) {
        if (op2.equals("(") || op2.equals(")"))
            return false;
        if ((op1.equals("*") || op1.equals("/") || op1.equals("%")) &&
                (op2.equals("+") || op2.equals("-")))
            return false;
        return true;
    }
}