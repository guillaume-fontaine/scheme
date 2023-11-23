import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static void initSchemeFunction(){
        SchemeEvaluatorV2.schemeFunctions.add(new SchemeFunction("+") {
            @Override
            int callFunction(Stack<Integer> integers) {
                int i = 0;
                for (Integer integer : integers) {
                    i += integer;
                }
                return i;
            }
        });
        SchemeEvaluatorV2.schemeFunctions.add(new SchemeFunction("-") {
            @Override
            int callFunction(Stack<Integer> integers) {
                int i = 0;
                for (Integer integer : integers) {
                    i -= integer;
                }
                return i;
            }
        });
        SchemeEvaluatorV2.schemeFunctions.add(new SchemeFunction("*") {
            @Override
            int callFunction(Stack<Integer> integers) {
                int i = 1;
                for (Integer integer : integers) {
                    i *= integer;
                }
                return i;
            }
        });
        SchemeEvaluatorV2.schemeFunctions.add(new SchemeFunction("/") {
            @Override
            int callFunction(Stack<Integer> integers) throws Exception {
                int i = 0;
                if(integers.size() != 2){
                    throw new Exception("Expected 2 arguments, got "+integers.size());
                }
                boolean firstValue = true;
                for (Integer integer : integers) {
                    if(firstValue){
                        firstValue = false;
                        i = integer;
                    }else {
                        i /= integer;
                    }
                }
                return i;
            }
        });
        SchemeEvaluatorV2.schemeFunctions.add(new SchemeFunction("modulo") {
            @Override
            int callFunction(Stack<Integer> integers) throws Exception {
                int i = 0;
                if(integers.size() != 2){
                    throw new Exception("Expected 2 arguments, got "+integers.size());
                }
                boolean firstValue = true;
                for (Integer integer : integers) {
                    if(firstValue){
                        firstValue = false;
                        i = integer;
                    }else {
                        i = i % integer;
                    }
                }
                return i;
            }
        });
    }

    public static void main(String[] args) {
        initSchemeFunction();
        String input = "(+ 1 2(* 3 4 5)(+ 1 (* 4 5 6))) (define (square xx)(* xx xx)) (square 5)";
        List<String> expressions = extractExpressions(input);

        for (String expression : expressions) {
            System.out.println(expression);
            new SchemeEvaluatorV2(expression).printResult();
        }
    }

    private static List<String> extractExpressions(String input) {
        List<String> expressions = new ArrayList<>();
        int openParentheses = 0;
        StringBuilder currentExpression = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '(') {
                openParentheses++;
            } else if (c == ')') {
                openParentheses--;
            }

            currentExpression.append(c);

            if (openParentheses == 0 && !currentExpression.isEmpty()) {
                expressions.add(currentExpression.toString().trim());
                currentExpression.setLength(0); // Réinitialiser la chaîne pour la prochaine expression
            }
        }

        return expressions;
    }

}
