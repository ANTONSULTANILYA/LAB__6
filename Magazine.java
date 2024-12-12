import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Magazine {

    private List<String> vars;

    public Magazine() {
        vars = new ArrayList<>();
    }

    private void getVars(String line) {
        String[] sections = line.trim().split(" ");
        if (!sections[0].equals("VAR")) {
            throw new Exceptions("Не найдено объявление переменных с помощью VAR");
        }
        String varsSection = line.substring(4).trim();
        int colonIndex = varsSection.lastIndexOf(':');
        if (colonIndex == -1 || colonIndex == 0) {
            throw new Exceptions("Отсутствует двоеточие перед объявлением типа переменных");
        }
        String varsString = varsSection.substring(0, colonIndex).trim();
        String[] varsArray = varsString.split(",");
        for (String var : varsArray) {
            var = var.trim();
            if (!var.matches("^[a-zA-Z]{1,9}$")) {
                throw new Exceptions("Неверно объявлена переменная");
            }
            vars.add(var);
        }
        String typeSection = varsSection.substring(colonIndex + 1).trim();
        if (!typeSection.equals("INTEGER;")) {
            throw new Exceptions("Не объявлен тип переменных или неверный формат (ожидается INTEGER;)");
        }
    }

    private void checkBegin(String line) {
        if (!line.equals("BEGIN")) {
            throw new Exceptions("Отсутствие ключевого слова BEGIN");
        }
    }

    private void checkAssign(List<String> lines) {
        boolean hasAssignment = false;
        for (int i = 2; i < lines.size() - 1; i++) {
            String line = lines.get(i).trim();
            String[] parts = line.split("=");
            if (parts.length != 2) {
                throw new Exceptions("Неверный формат присваивания");
            }
            if (!vars.contains(parts[0].trim())) {
                throw new Exceptions("Невозможно присвоить значение неинициализированной переменной");
            }
            String checkExpression = checkLine(parts[1]);
            if (!checkExpression.equals("OK")) {
                throw new Exceptions(checkExpression);
            }
            if (parts[1].charAt(parts[1].length() - 1) != ';') {
                throw new Exceptions("Нет закрывающего символа ;");
            }
            hasAssignment = true;
        }
        if(!hasAssignment){
            throw new Exceptions("Отсутствуют операции присваивания");
        }
    }

    private String checkLine(String line) {
        line = line.replaceAll("\\s+", "");
        if (line.isEmpty()) {
            throw new Exceptions("Строка пуста");
        }
        String tmp;
        if(line.charAt(line.length()-1) == ';'){
            tmp = line.substring(0, line.length()-1);
        }else {
            tmp = line;
        }
        Stack<Character> brackets = new Stack<>();
        boolean expectOperand = true;
        boolean expectUnaryminus = true;
        char prev = '\0';
        for (int i = 0; i < tmp.length(); i++) {
            char current = tmp.charAt(i);
            if (Character.isLetter(current)) {
                if (!expectOperand) {
                    throw new Exceptions("Не ожидался операнд");
                }
                int start = i;
                while (i < tmp.length() && Character.isLetter(tmp.charAt(i))) {
                    i++;
                }
                String variable = tmp.substring(start, i);
                if (!vars.contains(variable)) {
                    throw new Exceptions("Строка содержит неизвестную переменную");
                }
                i--;
                expectOperand = false;
                expectUnaryminus = false;
            } else if (Character.isDigit(current)) {
                if (!expectOperand) {
                    throw new Exceptions("Не ожидался операнд");
                }
                if(i+1<tmp.length() && Character.isDigit(tmp.charAt(i+1))){
                    throw new Exceptions("Допустимо использование только цифр в качестве констант");
                }
                expectOperand = false;
                expectUnaryminus = false;
            } else if (current == '(') {
                if (!expectOperand && prev != '(') {
                    throw new Exceptions("Неверное расположение открывающей скобки");
                }
                brackets.push(current);
                expectOperand = true;
                expectUnaryminus = true;
            } else if (current == ')') {
                if (expectOperand) {
                    throw new Exceptions("Закрывающая скобка не может стоять после оператора");
                }
                if (brackets.isEmpty()) {
                    throw new Exceptions("Неверно расставлены скобки");
                }
                brackets.pop();
                expectOperand = false;
                expectUnaryminus = true;
            } else if (current == '+' || current == '-' || current == '/') {
                if (expectOperand){
                    if(current == '-' && expectUnaryminus){
                        if (prev != '\0' && prev != '(') {
                            throw new Exceptions(
                                    "Унарный минус разрешён только в начале строки или внутри скобок"
                            );
                        }
                        expectUnaryminus = false;
                    }else{
                        throw new Exceptions("Ожидался операнд или унарный оператор с операндом");
                    }
                }else{
                    expectOperand = true;
                    expectUnaryminus = true;
                }
            } else {
                throw new Exceptions("Недопустимый символ");
            }
            prev = current;
        }
        if (!brackets.isEmpty()) {
            throw new Exceptions("Неверно расставлены скобки");
        }
        if(line.charAt(line.length()-1)!=';'){
            throw new Exceptions("Не обнаружен символ ;");
        }
        return "OK";
    }

    private void checkEnd(List<String> lines) {
        if ((lines.get(lines.size()-2).equals("END"))){
            throw new Exceptions("Введены символы после END");
        }
        if (!(lines.get(lines.size()-1)).equals("END")) {
            throw new Exceptions("Не обнаружен END");
        }
    }

    public String processProgram(List<String> list){
        try{
            if (list.isEmpty()){
                throw new Exceptions("Файл пуст");
            }
            getVars(list.get(0));
            checkBegin(list.get(1));
            checkAssign(list.subList(0, list.indexOf("END")+1));
            checkEnd(list);
            return "Программа введена корректно";
        }catch (Exceptions ex){
            return ex.getMessage();
        }
    }
}