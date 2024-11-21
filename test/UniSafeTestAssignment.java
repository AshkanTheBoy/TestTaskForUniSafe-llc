package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UniSafeTestAssignment {

    public static final double kMulX = 7.0843;
    public static final double kMulY = 7.0855;

    /*
     Это тестовое задание на собеседование в UniSafe llc
     На должность Junior Java Developer
     Чтобы задание считалось выполненным необходимо выполнить все 4 пункта
     Нам интересен твой подход к решению и условный % выполнения задания
     Вопросы можно задать тут galaev@team.usafe.ru

     В этом списке (listOfFigures) фигуры записанные координатами
     фигура может быть из 2 или 6 координат, это прямые и кривые, соответственно
     кривые фигуры записываются 5 координатами, потому что последняя 6ая - это первая координата следующей фигуры
     Все фигуры замкнуты
     List< ... > - список больших фигур
     List<List< ... > - список элементов одной фигуры
     List<List<List<Integer>>> - координаты элемента фигуры

     фигуры отправляются на плоттер и вырезаются на защитной пленке
     координаты для реза идут в том порядке, что и в списке
     соответственно есть начало реза по координатам и направление этого реза
     плоттер не всегда дорезает фигуры до конца, поэтому нужно всегда повторять первый элемент фигуры последним
     todo 1: напиши функцию чтобы добавлять первый элемент фигуры последним к каждой фигуре
     при вырезе мелкие фигуры могут задевать большие, поэтому порядок реза важен
     todo 2: напиши функцию чтобы изменить порядок реза фигур от самой маленькой к самой большой
     чтобы повысить качество реза нужно проделать несколько шагов
     чтобы нож не создавал брак пока разворачивает лезвие на большой градус,
     нужно чтобы все фигуры вырезались по часовой (изначально они случайны)
     todo 3: напиши функцию, которая разворачивает все фигуры по часовой меняя координаты местами
     у плоттеров есть особенность, нож которым вырезаются фигуры имеет направление
     todo 4: напиши функцию, которая меняет начало реза этой фигуры в направлении окончания реза предыдущей
     Например круг состоит из 5 кривых, это 4 четверти круга + 1 первая которую мы добавили в конце (чтобы круг хорошо прорезался).
     Если начало реза прошлого круга было справа относительно центра круга, то рез закончится через 5 четвертей, т.е. снизу.
     Таким образом последний элемент фигуры это кривая справа-вниз по часовой.
     После нее лезвие ножа направлено на лево.
     Соответственно, следующая фигура после этого круга должна начинаться в направлении лево.
    */

    public static void main(String[] args) {

        // todo замени путь к файлу
        String filePath = "camera_block.eps";

        List<List<List<Integer>>> listOfFigures = getFromEps(filePath);
        ShowList(listOfFigures);

        addFirstElToEachFigure(listOfFigures); //1
        System.out.println();
        ShowList(listOfFigures);

        rearrangeCordsInElements(listOfFigures); //2
        System.out.println();
        ShowList(listOfFigures);

        List<List<List<List<Integer>>>> figuresBySize;
        figuresBySize = groupFiguresBySize(listOfFigures); //3 группируем фигуры по площади
        for (List<List<List<Integer>>> list: figuresBySize){
            if (list.size()>1){
                //сортируем внутри группы, если кол-во 2 или более
                sortFiguresByCuttingAngles(list); //4
                //убираем лишние фигуры
                //нужны были для сортировки внутри группы
                list.removeFirst();
            }
        }
        listOfFigures.clear(); //очищаем и заполняем стартовый лист фигур заново
        for (List<List<List<Integer>>> list: figuresBySize){
            listOfFigures.addAll(list);
        }
        System.out.println();
        ShowList(listOfFigures);


    }

    public static void ShowList (List<List<List<Integer>>> listOfFigures) {
        for (List<List<Integer>> listOfFigure : listOfFigures) {
            System.out.println(listOfFigure);
        }
    }

    //напиши функцию чтобы добавлять первый элемент фигуры последним к каждой фигуре
    public static void addFirstElToEachFigure(List<List<List<Integer>>> listOfFigures){
        for (List<List<Integer>> listOfFigure : listOfFigures){
            listOfFigure.add(listOfFigure.getFirst());
        }
    }

    //Сортировка по кол-ву элементов фигуры (не по площади фигуры)
    public static void sortElementsBySize(List<List<List<Integer>>> listOfFigures){
        Comparator<List<List<Integer>>> comp = Comparator.comparing(List::size);
        listOfFigures.sort(comp);
    }
    //напиши функцию, которая разворачивает все фигуры по часовой меняя координаты местами
    /*
    Находим центральную точку у фигуры
    Находим вектор к ней от каждой вершины
    Находим векторное произведение для каждых двух последующих векторов и прибавляем к сумме
    Если сумма положительная - направление против часовой стрелки
    Если отрицательная - по часовой
     */
    public static void rearrangeCordsInElements(List<List<List<Integer>>> listOfFigures){
        //делаем плоский список для простоты
        List<Integer> allCords = new ArrayList<>();
        //ищем центральную точку
        int xCordsSum; //сумма х-координат
        int xCount; //кол-во х-координат
        int yCordsSum; //сумма у-координат
        int yCount; //кол-во у-координат
        double[] centroid = new double[2]; //центральная точка
        double[] currVectorToCenter = new double[2]; //текущий вектор к центру
        double[] nextVectorToCenter = new double[2]; //следующий вектор к центру
        for (List<List<Integer>> listOfElements: listOfFigures){
            xCordsSum = 0;
            xCount = 0;
            yCordsSum = 0;
            yCount = 0;
            allCords.clear();
            listOfElements.forEach(allCords::addAll);
            for (int i = 0; i<allCords.size();i++){
                if (i%2==0){
                    xCordsSum+=allCords.get(i);
                    xCount++;
                } else {
                    yCordsSum+=allCords.get(i);
                    yCount++;
                }
            }

            //получаем координаты центра фигуры
            centroid[0] = (double)xCordsSum/xCount;
            centroid[1] = (double)yCordsSum/yCount;

            int x1, y1, x2, y2;
            double result = 0;
            //ищем сумму всех векторных произведений
            for (int i = 0; i<allCords.size()-2;i+=2){
                //текущая координата
                x1 = allCords.get(i);
                y1 = allCords.get(i+1);
                //следующая координата
                x2 = allCords.get(i+2);
                y2 = allCords.get(i+3);

                //текущий вектор к центру
                currVectorToCenter[0] = x1-centroid[0];
                currVectorToCenter[1] = y1-centroid[1];

                //следующий вектор к центру
                nextVectorToCenter[0] = x2-centroid[0];
                nextVectorToCenter[1] = y2-centroid[1];

                //результат += векторное произведение данных двух
                result += (currVectorToCenter[0]*nextVectorToCenter[1])-
                        (currVectorToCenter[1]*nextVectorToCenter[0]);
//                System.out.println();
//                System.out.println(result);
//                System.out.println();
            }

//            System.out.println("FINAL RESULT-----");
//            System.out.println(result);
//            System.out.println();
            //разворачиваем, если результат положительный (больше нуля)
            if (result>0){
                reverseCuttingDirection(listOfElements);
            }
        }
    }

    public static void reverseCuttingDirection(List<List<Integer>> listOfElements){
        Integer temp1;
        Integer temp2;
        Collections.reverse(listOfElements); //разворачиваем лист элементов
        for (List<Integer> elements: listOfElements){
            if (elements.size()>2){ //свапаем первую и последнюю точку у кривых
                temp1 = elements.getFirst();
                temp2 = elements.get(1);
                elements.set(0,elements.get(4));
                elements.set(1,elements.get(5));
                elements.set(4,temp1);
                elements.set(5,temp2);
            }
        }
    }
    //напиши функцию, которая меняет начало реза этой фигуры в направлении окончания реза предыдущей
    public static void sortFiguresByCuttingAngles(List<List<List<Integer>>> listOfFigures){
        Map<Integer,List<List<Integer>>> nextCords = new HashMap<>(); //мапа стартовых точек из данной группы фигур
        int index = 0;
        for (List<List<Integer>> listOfElements: listOfFigures){
            nextCords.put(index,listOfElements);
//            System.out.println("MAP INDEX:");
//            System.out.println(index);
            index++;
//            System.out.println("MAP INDEX:");
        }
        int i = 0;
        int nextCordIndex;
        for (;i+1< listOfFigures.size();i++){
            nextCordIndex = rearrangeTwoFigures(listOfFigures,nextCords,i); //номер следующей фигуры в листе
                List<List<Integer>> tempFigure = listOfFigures.get(i+1);
                List<List<Integer>> nextFigure = listOfFigures.get(nextCordIndex);
                //свапаем след. и искомую фигуры местами
                listOfFigures.set(i+1,nextFigure);
                listOfFigures.set(nextCordIndex,tempFigure);
                //то же самое в мапе фигур
                nextCords.put(i+1,nextFigure);
                nextCords.put(nextCordIndex,tempFigure);
                //удаляем текущую фигуру из мапы
                nextCords.remove(i);
//                System.out.println("___________");
//                ShowList(listOfFigures);
//                System.out.println("___________");
        }
    }
    //главный метод для вычисления угла
    public static int rearrangeTwoFigures(List<List<List<Integer>>> listOfFigures,Map<Integer,List<List<Integer>>> nextCords, int i){
        //предпосленяя точка
        List<Integer> lastElem = listOfFigures.get(i).get(listOfFigures.get(i).size()-2);
        int lastCordX = lastElem.get(lastElem.size()-2);
        int lastCordy = lastElem.getLast();
        //последняя точка
        List<Integer> currElem = listOfFigures.get(i).getFirst();
//        System.out.println("CURRENT FIGURE INDEX "+i+" "+currElem.getFirst()+" "+currElem.get(1));
        int currCordX = currElem.getFirst();
        int currCordY = currElem.get(1);

        //ищем вектор от предпоследней до последней точки
        int[] firstVector = {currCordX-lastCordX,currCordY-lastCordy}; //первый вектор
        Map<Integer, Double> angles = new HashMap<>();

        //идем по листу стартовых точек
        for (Map.Entry<Integer,List<List<Integer>>>entry:nextCords.entrySet()){
            int j = entry.getKey();
            List<List<Integer>> value = entry.getValue();
            List<Integer> currCords = value.getFirst();
            //скипаем, если точка совпадает с текущей
            if (currCords.getFirst().equals(currCordX)&&
                    currCords.get(1).equals(currCordY)){
                continue;
            }
            //следующая точка
            int nextCordX = value.getFirst().getFirst();
            int nextCordY = value.getFirst().get(1);
            //следующий вектор
            int[] nextVector = {nextCordX-currCordX,nextCordY-currCordY};

            //заполняем список углов
//            System.out.println(j+" "+nextCordX+" "+nextCordY);
            angles.put(j,calculateAngle(firstVector,nextVector));
        }
        //возвращаем индекс фигуры, к которой нужен наименьший угол поворота
        return getLeastAngleIndex(angles);
    }

    public static int getLeastAngleIndex(Map<Integer,Double> angles){
        double finalAngle = Math.PI; //наименьший угол
        int finalIndex = 0; //искомый номер фигуры
        for (Map.Entry<Integer,Double>entry:angles.entrySet()){
//            System.out.println(entry.getKey()+" "+entry.getValue());
            if (entry.getValue()<finalAngle||entry.getValue()>3.14){ //если угол меньше, то обновляем искомую фигуру
                finalAngle = entry.getValue();
                finalIndex = entry.getKey();
            }
        }
//        System.out.println("TARGET ANGLE "+finalIndex+" "+finalAngle);
        return finalIndex;
    }

    public static double calculateAngle(int[] firstVector, int[] nextVector){
        //сумма произведений
        int dotProduct = (nextVector[0]*firstVector[0])+(nextVector[1]*firstVector[1]);
        //произведение магнитуд векторов
        double firstMagnitude = Math.sqrt((firstVector[0]*firstVector[0])+(firstVector[1]*firstVector[1]));
        double nextMagnitude = Math.sqrt((nextVector[0]*nextVector[0])+(nextVector[1]*nextVector[1]));
        double finalMagnitude = firstMagnitude*nextMagnitude;
        //вычисление угла и ограничение числа до [-1.0,1.0]
        return Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct / finalMagnitude)));
    }

    //группируем фигуры по кол-ву элементов для универсальности
    public static List<List<List<List<Integer>>>> groupFiguresBySize(List<List<List<Integer>>> listOfFigures){
        List<List<List<List<Integer>>>> figuresBySize = new ArrayList<>();
        Map<Double,List<List<List<Integer>>>> mapOfFigures = new TreeMap<>();
        int sizeRounding = 300;
        int index = 0;
        double area;
        double prevArea = 0;
        for (int i=0; i< listOfFigures.size();i++){
            area = calculateAreaByGauss(listOfFigures.get(i));
//            System.out.println();
//            System.out.println("FIGURE AREA: "+i+" "+area);
//            System.out.println();
            double difference = Math.abs(area-prevArea);
            if (difference>sizeRounding){ //добавляем новую группу фигур, если размер текущей превышает погрешность
                prevArea = area;
                mapOfFigures.put(prevArea,new ArrayList<>());
                mapOfFigures.get(prevArea).add(listOfFigures.get(i));
            } else {
                mapOfFigures.get(prevArea).add(listOfFigures.get(i));
            }
        }
        //собираем отсортированные фигуры в список(проще обрабатывать дальше)
        for (Map.Entry<Double,List<List<List<Integer>>>> entry: mapOfFigures.entrySet()){
            figuresBySize.add(new ArrayList<>());
            for (List<List<Integer>> list: entry.getValue()){
                figuresBySize.get(index).add(list);
            }
            index++;
        }
        //добавляем к каждой группе, если она не из 1-й фигуры и, если она не первая в списке, предыдущую фигуру
        //тогда работает мой алгоритм, ибо он считает углы в группе
        for (List<List<List<Integer>>> group:figuresBySize){
            if (figuresBySize.indexOf(group)!=0&&group.size()>1){
                List<List<Integer>> lastFigure = figuresBySize.get(figuresBySize.indexOf(group)-1).getLast();
                group.addFirst(lastFigure);
            }
        }
//        for (List<List<List<Integer>>> group:figuresBySize){
//            System.out.println();
//            ShowList(group);
//            System.out.println();
//        }
        return figuresBySize;
    }
    //считаем площадь по формуле Гаусса
    public static double calculateAreaByGauss(List<List<Integer>> listOfElements){
        double area;
        int sumOfDifferences = 0;
        int currentX,currentY,nextX,nextY;
        List<Integer> allCords = new ArrayList<>();
        for (List<Integer> elems: listOfElements){ //сделал плоский список координат
            allCords.addAll(elems);
        }
        //берем координаты по индексам
        for (int i = 0; i < allCords.size()-2; i+=2) {
            currentX = allCords.get(i);
            currentY = allCords.get(i+1);
            nextX = allCords.get(i+2);
            nextY = allCords.get(i+3);
            sumOfDifferences += (currentX*nextY) - (currentY*nextX);
        }
        area = Math.abs(sumOfDifferences)/2.0;
        return area;
    }



    public static List<List<List<Integer>>> getFromEps(String filePath){
        List<List<List<Integer>>> listOfFigures = new ArrayList<>();

        List<String> blocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean reachedEndData = false;
            boolean reachedBeginData = false;
            boolean blockStarted = false;

            while ((line = reader.readLine()) != null) {
                if (!reachedBeginData) {
                    if (line.trim().startsWith("%%EndPageSetup")) {
                        reachedBeginData = true;
                    }
                } else if (!reachedEndData) {

                    if (line.startsWith("%ADO")) {
                        reachedEndData = true;
                    } else {
                        if(line.contains("mo") && Character.isDigit(line.charAt(0))){
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.contains("m") && Character.isDigit(line.charAt(0))) {
                            listOfFigures.add(new ArrayList<>());
                            blockStarted = true;
                            blocks.add(line);
                        } else if (line.trim().equals("cp") || line.trim().equals("@c") || line.trim().equals("@")) {
                            blockStarted = false;
                        } else if (blockStarted) {
                            blocks.add(line);
                        }
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading EPS file: " + e.getMessage());
            return new ArrayList<>();
        }

        int current_figure = -1;
        for (String block : blocks) {
            String[] line_parts = block.split(" ");

            if (Objects.equals(line_parts[line_parts.length - 1], "mo") || Objects.equals(line_parts[line_parts.length - 1], "m")) {
                List<Integer> listN = new ArrayList<>();
                current_figure++;
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "li")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            } else if (Objects.equals(line_parts[line_parts.length - 1], "cv") || Objects.equals(line_parts[line_parts.length - 1], "C")) {
                List<Integer> listN = new ArrayList<>();
                getNumericalWithDot(current_figure, listOfFigures, line_parts, listN);
            }
        }

        removeEmptyLists(listOfFigures);
        removeNotCycledFigures(listOfFigures);

        return listOfFigures;
    }

    public static void removeEmptyLists(List<List<List<Integer>>> listOfFigures) {
        listOfFigures.removeIf(List::isEmpty);
    }
    public static void removeNotCycledFigures(List<List<List<Integer>>> listOfFigures) {
        Iterator<List<List<Integer>>> iterator = listOfFigures.iterator();
        while (iterator.hasNext()) {
            List<List<Integer>> listOfFigure = iterator.next();
            int last_x = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 2);
            int last_y = listOfFigure.get(listOfFigure.size() - 1).get(listOfFigure.get(listOfFigure.size() - 1).size() - 1);
            int first_x = listOfFigure.get(0).get(0);
            int first_y = listOfFigure.get(0).get(1);
            if (first_x != last_x || first_y != last_y) {
                iterator.remove();
            }
        }
    }

    private static void getNumericalWithDot(int current_figure, List<List<List<Integer>>> listOfFigures, String[] line_parts, List<Integer> listN) {
        for (int j = 0; j < line_parts.length - 1; j++) {
            if (line_parts[j].startsWith(".")) {
                line_parts[j] = "0" + line_parts[j];
            }
            double calk;
            if (j % 2 != 0) {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulX;
            } else {
                calk = (Double.parseDouble(line_parts[j]) + 1.5) * kMulY;
            }
            int this_int = (int) Math.round(calk);
            listN.add(this_int);
        }
        listOfFigures.get(current_figure).add(listN);
    }

}
