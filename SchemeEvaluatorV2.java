import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SchemeEvaluatorV2 {
    private int length;
    private char[] charArray;
    private int pointer;

    public static List<SchemeFunction> schemeFunctions = new ArrayList<>();

    private Integer result;

    public SchemeEvaluatorV2(String input) {
        //"(define (square x) (* x x))";
        initializeGlobals(input);

        initializeSchemeFunctions();

        result = evaluateExpression();
    }

    public void printResult(){
        if(result != null) System.out.println(result);
    }

    public Integer getResult(){
        return result;
    }

    public static void main(String[] args) {
    }

    private void initializeGlobals(String input) {
        length = input.length();
        charArray = input.toCharArray();
        pointer = 0;
    }

    private void initializeSchemeFunctions() {
        // Votre initialisation des fonctions Scheme
    }

    private Integer evaluateExpression() {
        String operator = extractOperator();
        System.out.println("Operator = " + operator);

        if ("define".equals(operator)) {
            // Gérer la définition d'une nouvelle fonction
            defineFunction();
            // Après la définition, réévaluer l'expression
            return null;
        }

        Stack<Integer> operands = extractOperands();
        System.out.println(operands);

        // Si l'opérateur est une fonction Scheme, effectuer le calcul
        if (isSchemeFunction(operator)) {
            return calculateSchemeFunction(operator, operands);
        }

        // Sinon, il s'agit d'un opérateur standard, nous devons effectuer le calcul
        return 0;
    }

    private void defineFunction() {
        // Extrayez le nom et les paramètres de la nouvelle fonction
        skipWhiteSpace();
        String functionName = extractOperator();
        System.out.println("Function Name = " + functionName);

        // Les paramètres de la fonction
        Stack<String> parameters = new Stack<>();
        while (charArray[pointer] != ')') {
            skipWhiteSpace();
            if (charArray[pointer] != '(') {
                // Extraire le nom du paramètre
                parameters.push(extractOperator());
            }

        }

        // Ignorer la parenthèse fermante
        pointer++;

        // Extraire le corps de la fonction
        String function = extractFunction();
        System.out.println("Function = " + function);

        // Ajouter la nouvelle fonction à la liste
        schemeFunctions.add(new SchemeFunction(functionName) {
            @Override
            int callFunction(Stack<Integer> integers) throws Exception {
                if(integers.size() != parameters.size()){
                    throw new Exception("Expected "+parameters.size()+" arguments, got "+integers.size());
                }
                StringBuilder parsedFunction = new StringBuilder(function);
                // Associez les paramètres avec les valeurs fournies lors de l'appel
                for (int i = 0; i < parameters.size(); i++) {
                    // Remplacez le paramètre dans le corps de la fonction
                    String parameter = parameters.get(i);
                    String value = integers.get(i).toString();
                    replaceParameter(parsedFunction, parameter, value);
                }
                // Évaluez le corps de la fonction
                return evaluateBody(parsedFunction.toString());
            }
        });
    }

    private void replaceParameter(StringBuilder parsedFunction, String parameter, String value) {
        int index = parsedFunction.indexOf(parameter);
        while (index != -1) {
            parsedFunction.replace(index, index + parameter.length(), value);
            index = parsedFunction.indexOf(parameter, index + value.length());
        }
    }


    private int evaluateBody(String body) {
        // Évaluez le corps de la fonction
        // Utilisez la même logique que pour évaluerExpression
        // en ajustant le pointeur et en appelant evaluateExpression
        return new SchemeEvaluatorV2(body).getResult(); // Valeur par défaut
    }

    private void addVariable(String name, int value) {
        // Logique pour ajouter une variable locale lors de l'appel de la fonction
        // Cela pourrait impliquer de stocker les variables dans un cadre d'exécution spécifique
        // ou en les passant comme paramètres à la fonction évaluée.
    }


    private String extractOperator() {
        skipWhiteSpace();
        skipOpenParenthesis();
        int startIndex = pointer;
        while (pointer < length && charArray[pointer] != ' ' && charArray[pointer] != '(' && charArray[pointer] != ')') {
            pointer++;
        }
        return new String(charArray, startIndex, pointer - startIndex);
    }


    private String extractFunction() {
        int openParentheses = 0;
        StringBuilder currentExpression = new StringBuilder();
        skipWhiteSpace();
        while (pointer < length) {
            if (charArray[pointer] == '(') {
                openParentheses++;
            } else if (charArray[pointer] == ')') {
                openParentheses--;
            }

            currentExpression.append(charArray[pointer]);

            if (openParentheses == 0 && !currentExpression.isEmpty()) {
                return currentExpression.toString().trim();
            }
            pointer++;
        }
        return currentExpression.toString().trim();
    }

    private  Stack<Integer> extractOperands() {
        Stack<Integer> operands = new Stack<>();
        skipWhiteSpace();

        while (pointer < length && charArray[pointer] != ')') {
            if (charArray[pointer] == '(') {
                pointer++;
                operands.push(evaluateExpression()); // Appel récursif pour évaluer les sous-expressions
            } else {
                int startIndex = pointer;
                while (pointer < length && charArray[pointer] != ' ' && charArray[pointer] != ')' && charArray[pointer] != '(') {
                    pointer++;
                }
                operands.push(Integer.parseInt(new String(charArray, startIndex, pointer - startIndex)));
            }

            skipWhiteSpace();
        }

        pointer++; // Passer à la caractère après la parenthèse fermante
        return operands;
    }

    private boolean isSchemeFunction(String operator) {
        return schemeFunctions.stream().anyMatch(f -> f.getName().equals(operator));
    }

    private int calculateSchemeFunction(String operator, Stack<Integer> operands) {
        try {
            for (SchemeFunction scheme : schemeFunctions) {
                if (scheme.getName().equals(operator)) {
                    return scheme.callFunction(operands);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;  // Valeur par défaut si la fonction n'est pas trouvée ou si une exception est levée
    }


    // Le reste du code reste inchangé

    private void skipWhiteSpace() {
        while (pointer < length && Character.isWhitespace(charArray[pointer])) {
            pointer++;
        }
    }

    private void skipOpenParenthesis() {
        while (pointer < length && charArray[pointer] == '(') {
            pointer++;
        }
    }
}
