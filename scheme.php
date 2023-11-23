<?php

abstract class SchemeFunction {
    private $name;
    private $parameters;
    private $function;

    public function __construct($name, $parameters = array(), $function = "") {
        $this->name = $name;
        $this->parameters = $parameters;
        $this->function = $function;
    }

    abstract public function callFunction($integers);

    public function getName() {
        return $this->name;
    }

    public function setName($name) {
        $this->name = $name;
    }

    public function getParameters() {
        return $this->parameters;
    }

    public function getFunction() {
        return $this->function;
    }
}

class SchemeEvaluatorV2 {
    private $length;
    private $charArray;
    private $pointer;

    public static $schemeFunctions = array();

    private $result;

    public function __construct($input) {
        $this->initializeGlobals($input);

        $this->initializeSchemeFunctions();

        $this->result = $this->evaluateExpression();
    }

    public function printResult() {
        if ($this->result !== null && $this->result !== 0) {
            echo $this->result . "\n";
        }
    }

    public function getResult() {
        return $this->result;
    }

    private function initializeGlobals($input) {
        $this->length = strlen($input);
        $this->charArray = str_split($input);
        $this->pointer = 0;
    }

    private function initializeSchemeFunctions() {
        // Votre initialisation des fonctions Scheme
    }

    private function evaluateExpression() {
        $operator = $this->extractOperator();
        //echo "Operator = " . $operator . "\n";

        if ($operator === "define") {
            $this->defineFunction();
            return null;
        }

        $operands = $this->extractOperands();
        //print_r($operands);

        if ($this->isSchemeFunction($operator)) {
            return $this->calculateSchemeFunction($operator, $operands);
        }

        return 0;
    }

    private function defineFunction() {
        $this->skipWhiteSpace();
        $functionName = $this->extractOperator();
        //echo "Function Name = " . $functionName . "\n";

        $parameters = array();
        while ($this->charArray[$this->pointer] !== ')') {
            $this->skipWhiteSpace();
            if ($this->charArray[$this->pointer] !== '(') {
                $parameters[] = $this->extractOperator();
            }
        }

        $this->pointer++;

        $function = $this->extractFunction();
        //echo "Function = " . $function . "\n";

        self::$schemeFunctions[] = new class ($functionName, $parameters, $function) extends SchemeFunction {
            public function callFunction($integers) {
                if (count($integers) !== count($this->getParameters())) {
                    throw new Exception("Expected " . count($this->getParameters()) . " arguments, got " . count($integers));
                }

                $parsedFunction = $this->getFunction();

                for ($i = 0; $i < count($this->getParameters()); $i++) {
                    $parameter = $this->getParameters()[$i];
                    $value = $integers[$i];
                    $parsedFunction = SchemeEvaluatorV2::replaceParameter($parsedFunction, $parameter, $value);
                }

                return SchemeEvaluatorV2::evaluateBody($parsedFunction);
            }
        };
    }

    public static function replaceParameter($parsedFunction, $parameter, $value) {
        $index = strpos($parsedFunction, $parameter);

        while ($index !== false) {
            $parsedFunction = substr_replace($parsedFunction, $value, $index, strlen($parameter));
            $index = strpos($parsedFunction, $parameter, $index + strlen($value));
        }

        return $parsedFunction;
    }

    public static function evaluateBody($body) {
        return (new SchemeEvaluatorV2($body))->getResult();
    }

    private function addVariable($name, $value) {
        // Logique pour ajouter une variable locale lors de l'appel de la fonction
    }

    private function extractOperator() {
        $this->skipWhiteSpace();
        $this->skipOpenParenthesis();
        $startIndex = $this->pointer;

        while ($this->pointer < $this->length && !in_array($this->charArray[$this->pointer], [' ', '(', ')'])) {
            $this->pointer++;
        }

        return implode('', array_slice($this->charArray, $startIndex, $this->pointer - $startIndex));
    }

    private function extractFunction() {
        $openParentheses = 0;
        $currentExpression = '';

        $this->skipWhiteSpace();

        while ($this->pointer < $this->length) {
            if ($this->charArray[$this->pointer] === '(') {
                $openParentheses++;
            } elseif ($this->charArray[$this->pointer] === ')') {
                $openParentheses--;
            }

            $currentExpression .= $this->charArray[$this->pointer];

            if ($openParentheses === 0 && !empty($currentExpression)) {
                return trim($currentExpression);
            }

            $this->pointer++;
        }

        return trim($currentExpression);
    }

    private function extractOperands() {
        $operands = array();
        $this->skipWhiteSpace();

        while ($this->pointer < $this->length && $this->charArray[$this->pointer] !== ')') {
            if ($this->charArray[$this->pointer] === '(') {
                $this->pointer++;
                $operands[] = ($this->evaluateExpression());
            } else {
                $startIndex = $this->pointer;

                while ($this->pointer < $this->length && !in_array($this->charArray[$this->pointer], [' ', ')', '('])) {
                    $this->pointer++;
                }

                $operands[] = ((int)implode('', array_slice($this->charArray, $startIndex, $this->pointer - $startIndex)));
            }

            $this->skipWhiteSpace();
        }

        $this->pointer++;

        return $operands;
    }

    private function isSchemeFunction($operator) {
        foreach (self::$schemeFunctions as $scheme) {
            if ($scheme->getName() === $operator) {
                return true;
            }
        }

        return false;
    }

    private function calculateSchemeFunction($operator, $operands) {
        try {
            foreach (self::$schemeFunctions as $scheme) {
                if ($scheme->getName() === $operator) {
                    return $scheme->callFunction($operands);
                }
            }
        } catch (Exception $e) {
            echo $e->getMessage() . "\n";
        }

        return 0;
    }

    private function skipWhiteSpace() {
        while ($this->pointer < $this->length && ctype_space($this->charArray[$this->pointer])) {
            $this->pointer++;
        }
    }

    private function skipOpenParenthesis() {
        while ($this->pointer < $this->length && $this->charArray[$this->pointer] === '(') {
            $this->pointer++;
        }
    }
}

class Main {
    public static function initSchemeFunction() {
        SchemeEvaluatorV2::$schemeFunctions[] = new class ("+") extends SchemeFunction {
            public function callFunction($integers) {
                $i = 0;
                foreach ($integers as $integer) {
                    $i += $integer;
                }
                return $i;
            }
        };
        SchemeEvaluatorV2::$schemeFunctions[] = new class ("-") extends SchemeFunction {
            public function callFunction($integers) {
                $i = 0;
                foreach ($integers as $integer) {
                    $i -= $integer;
                }
                return $i;
            }
        };
        SchemeEvaluatorV2::$schemeFunctions[] = new class ("*") extends SchemeFunction {
            public function callFunction($integers) {
                $i = 1;
                foreach ($integers as $integer) {
                    $i *= $integer;
                }
                return $i;
            }
        };
        SchemeEvaluatorV2::$schemeFunctions[] = new class ("/") extends SchemeFunction {
            public function callFunction($integers) {
                $i = 0;

                if (count($integers) !== 2) {
                    throw new Exception("Expected 2 arguments, got " . count($integers));
                }

                $firstValue = true;

                foreach ($integers as $integer) {
                    if ($firstValue) {
                        $firstValue = false;
                        $i = $integer;
                    } else {
                        if ($integer === 0) {
                            throw new Exception("Division by zero");
                        }
                        $i /= $integer;
                    }
                }

                return $i;
            }
        };
        SchemeEvaluatorV2::$schemeFunctions[] = new class ("modulo") extends SchemeFunction {
            public function callFunction($integers) {
                $i = 0;

                if (count($integers) !== 2) {
                    throw new Exception("Expected 2 arguments, got " . count($integers));
                }

                $firstValue = true;

                foreach ($integers as $integer) {
                    if ($firstValue) {
                        $firstValue = false;
                        $i = $integer;
                    } else {
                        if ($integer === 0) {
                            throw new Exception("Modulo by zero");
                        }
                        $i = $i % $integer;
                    }
                }

                return $i;
            }
        };        
        // Repeat similar structure for other SchemeFunctions
    }

    public static function main() {
        self::initSchemeFunction();
        $input = "(+ 1 2(* 3 4 5)(+ 1 (* 4 5 6))) (define (square xx)(* xx xx)) (square 5)";
        $expressions = self::extractExpressions($input);

        foreach ($expressions as $expression) {
            echo $expression . "\n";
            (new SchemeEvaluatorV2($expression))->printResult();
        }
    }

    private static function extractExpressions($input) {
        $expressions = array();
        $openParentheses = 0;
        $currentExpression = "";

        foreach (str_split($input) as $c) {
            if ($c === '(') {
                $openParentheses++;
            } elseif ($c === ')') {
                $openParentheses--;
            }

            $currentExpression .= $c;

            if ($openParentheses === 0 && !empty($currentExpression)) {
                $expressions[] = $currentExpression;
                $currentExpression = ""; // Réinitialiser la chaîne pour la prochaine expression
            }
        }

        return $expressions;
    }
}

Main::main();

?>
